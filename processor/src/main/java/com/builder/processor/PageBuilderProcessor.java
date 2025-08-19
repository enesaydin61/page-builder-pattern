package com.builder.processor;

import com.builder.annotations.GenerateMethods;
import com.builder.errors.DuplicateElementException;
import com.builder.util.PageUtil;
import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.openqa.selenium.support.FindBy;

@SupportedAnnotationTypes({"com.builder.annotations.PageBuilder"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class PageBuilderProcessor extends AbstractProcessor {

  private Trees trees;
  private TreeMaker maker;
  private Names names;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    try {
      this.trees = Trees.instance(processingEnv);
      var ctx = ((JavacProcessingEnvironment) processingEnv).getContext();
      this.maker = TreeMaker.instance(ctx);
      this.names = Names.instance(ctx);
    } catch (Throwable t) {
      processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
          "Failed to initialize javac internals: " + t.getMessage());
      throw t;
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    annotations.forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation)
        .forEach(element -> {
          try {
            checkElements(element);
            injectIntoExistingClass(element);
          } catch (RuntimeException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                "PageBuilderProcessor failed: " + ex.getMessage(), element);
            throw ex;
          }
        }));
    return true;
  }

  private void injectIntoExistingClass(Element element) {
    JCTree tree = (JCTree) trees.getTree(element);
    if (!(tree instanceof JCTree.JCClassDecl cls)) {
      return;
    }

    // Ensure @Page annotation exists on the class
    addPageAnnotationIfAbsent(cls);

    // Generate methods based on @GenerateMethods on fields
    element.getEnclosedElements().stream()
        .filter(e -> ElementKind.FIELD.equals(e.getKind()))
        .map(VariableElement.class::cast)
        .forEach(field -> {
          GenerateMethods gm = field.getAnnotation(GenerateMethods.class);
          if (gm == null) return;

          String fieldName = field.getSimpleName().toString();
          String cap = capitalize(fieldName);

          if (gm.click()) addClickMethod(cls, fieldName, cap, gm);
          if (gm.sendKeys()) addSendKeysMethod(cls, fieldName, cap, gm);
          if (gm.isDisplayed()) addIsDisplayedMethod(cls, fieldName, cap);
          if (gm.getText()) addGetTextMethod(cls, fieldName, cap);
          if (gm.selectByText()) addSelectByTextMethod(cls, fieldName, cap);
          if (gm.selectByIndex()) addSelectByIndexMethod(cls, fieldName, cap);
        });
  }

  private void addPageAnnotationIfAbsent(JCTree.JCClassDecl cls) {
    boolean exists = cls.mods.annotations.stream()
        .anyMatch(a -> a.annotationType.toString().equals("com.builder.annotations.component.Page"));
    if (exists) return;

    var pageAnno = maker.Annotation(memberAccess("com.builder.annotations.component.Page"), List.nil());
    cls.mods.annotations = cls.mods.annotations.append(pageAnno);
  }

  private void addClickMethod(JCTree.JCClassDecl cls, String field, String cap, GenerateMethods gm) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) return;

    Optional<String> returnPageFqn = getReturnPageFqn(gm);

    if (returnPageFqn.isPresent()) {
      var returnType = memberAccess(returnPageFqn.get());
      var callClick = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("click")),
              List.of(maker.Ident(names.fromString(field)))));

      var getBeanCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
          List.of(maker.Select(returnType, names._class)));

      var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.nil(),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    } else {
      var returnType = maker.Ident(cls.name);

      var callClick = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("click")),
              List.of(maker.Ident(names.fromString(field)))));

      var body = maker.Block(0, List.of(callClick, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.nil(),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addSendKeysMethod(JCTree.JCClassDecl cls, String field, String cap, GenerateMethods gm) {
    String methodName = "sendKeys" + cap;
    if (hasMethod(cls, methodName)) return;

    Optional<String> returnPageFqn = getReturnPageFqn(gm);

    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("text"), stringType, null);

    if (returnPageFqn.isPresent()) {
      var returnType = memberAccess(returnPageFqn.get());

      var callSendKeys = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("sendKeys")),
              List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("text")))));

      var getBeanCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
          List.of(maker.Select(returnType, names._class)));

      var body = maker.Block(0, List.of(callSendKeys, maker.Return(getBeanCall)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(param),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    } else {
      var returnType = maker.Ident(cls.name);

      var callSendKeys = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("sendKeys")),
              List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("text")))));

      var body = maker.Block(0, List.of(callSendKeys, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(param),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addIsDisplayedMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "isDisplayed" + cap;
    if (hasMethod(cls, methodName)) return;

    var returnType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.BOOLEAN);

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")), names.fromString("isDisplayed")),
        List.of(maker.Ident(names.fromString(field))));

    var body = maker.Block(0, List.of(maker.Return(call)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.nil(),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetTextMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "getText" + cap;
    if (hasMethod(cls, methodName)) return;

    var returnType = memberAccess("java.lang.String");

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")), names.fromString("getText")),
        List.of(maker.Ident(names.fromString(field))));

    var body = maker.Block(0, List.of(maker.Return(call)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.nil(),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addSelectByTextMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "selectByText" + cap;
    if (hasMethod(cls, methodName)) return;

    var returnType = maker.Ident(cls.name);
    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("text"), stringType, null);

    var call = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("selectByText")),
            List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("text")))));

    var body = maker.Block(0, List.of(call, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(param),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addSelectByIndexMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "selectByIndex" + cap;
    if (hasMethod(cls, methodName)) return;

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"), intType, null);

    var call = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("selectByIndex")),
            List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("index")))));

    var body = maker.Block(0, List.of(call, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(param),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private boolean hasMethod(JCTree.JCClassDecl cls, String methodName) {
    for (JCTree def : cls.defs) {
      if (def instanceof JCTree.JCMethodDecl m && m.getName().toString().equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  private JCTree.JCExpression memberAccess(String fqn) {
    String[] parts = fqn.split("\\.");
    JCTree.JCExpression expr = maker.Ident(names.fromString(parts[0]));
    for (int i = 1; i < parts.length; i++) {
      expr = maker.Select(expr, names.fromString(parts[i]));
    }
    return expr;
  }

  private Optional<String> getReturnPageFqn(GenerateMethods gm) {
    java.util.List<? extends TypeMirror> mirrors = PageUtil.getTypeMirrorFromAnnotationValue(() -> gm.returnPage());
    if (mirrors == null || mirrors.isEmpty()) return Optional.empty();
    String fqn = mirrors.get(0).toString();
    if (Objects.equals(fqn, Nullable.class.getName())) {
      return Optional.empty();
    }
    return Optional.of(fqn);
  }

  private String capitalize(String name) {
    if (name == null || name.isEmpty()) return name;
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  private void checkElements(Element element) {
    java.util.List<? extends Element> fields = element
        .getEnclosedElements()
        .stream()
        .filter(e -> ElementKind.FIELD.equals(e.getKind())).toList();

    HashMap<String, String> selectorMap = new HashMap<>();

    fields.forEach(field -> {
      if (field.asType().toString().contains("WebElement")
          && field.getAnnotation(FindBy.class) != null) {
        var findBy = field.getAnnotation(FindBy.class);

        Arrays.asList(
                findBy.id(),
                findBy.className(),
                findBy.name(),
                findBy.css(),
                findBy.xpath()
            )
            .stream()
            .filter(selector -> !selector.isEmpty())
            .forEach(find -> selectorMap.put(field.getSimpleName().toString(), find));
      }
    });

    selectorMap.forEach((name, selector) -> {
      var size = Collections.frequency(selectorMap.values(), selector);
      if (size > 1) {
        throw new DuplicateElementException("Duplicate Elements !"
            .concat("\n")
            .concat("Class : ".concat(element.asType().toString()))
            .concat("\n")
            .concat("Element : ".concat(name))
            .concat("\n")
            .concat("Selector : ".concat(selector)));
      }
    });
  }
}