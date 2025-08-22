package com.builder.processor;

import com.builder.annotations.GenerateMethods;
import com.builder.errors.DuplicateElementException;
import com.builder.util.PageUtil;
import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.TypeTag;
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

@SupportedAnnotationTypes({"com.builder.annotations.LayoutBuilder"})
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
          if (gm == null) {
            return;
          }

          String fieldName = field.getSimpleName().toString();
          String cap = capitalize(fieldName);

          if (gm.click()) {
            if (isListWebElement(field)) {
              addListClickMethod(cls, fieldName, cap, gm, field);
            } else {
              addClickMethod(cls, fieldName, cap, gm);
            }
          }

          if (gm.jexeClick()) {
            if (isListWebElement(field)) {
              addListJexeClickMethod(cls, fieldName, cap, gm, field);
            } else {
              addJexeClickMethod(cls, fieldName, cap, gm, field);
            }
          }

          if (gm.genericReturnJexeClick()) {
            addGenericReturnJexeClickMethod(cls, fieldName, cap);
          }

          if (gm.genericReturnClick()) {
            if (isListWebElement(field)) {
              addListGenericReturnClickMethod(cls, fieldName, cap, field);
            } else {
              addGenericReturnClickMethod(cls, fieldName, cap);
            }
          }

          if (gm.mouseHover()) {
            if (isListWebElement(field)) {
              addListMouseHoverMethod(cls, fieldName, cap);
            } else {
              addMouseHoverMethod(cls, fieldName, cap);
            }
          }
          if (gm.sendKeys()) {
            addSendKeysMethod(cls, fieldName, cap, gm);
          }
          if (gm.clear()) {
            addClearMethod(cls, fieldName, cap);
          }

          if (gm.isDisplayed()) {
            if (isListWebElement(field)) {
              addListIsDisplayedMethod(cls, fieldName, cap, field);
            } else {
              addIsDisplayedMethod(cls, fieldName, cap);
            }
          }

          if (gm.isDisplayedTimeout()) {
            if (isListWebElement(field)) {
              addListIsDisplayedTimeoutMethod(cls, fieldName, cap, field);
            } else {
              addIsDisplayedTimeoutMethod(cls, fieldName, cap);
            }
          }

          if (gm.getText()) {
            if (isListWebElement(field)) {
              addListGetTextMethod(cls, fieldName, cap);
            } else {
              addGetTextMethod(cls, fieldName, cap);
            }
          }

          if (gm.getListText()) {
            addGetListTextMethod(cls, fieldName, cap, field);
          }

          if (gm.getAttribute()) {
            if (isListWebElement(field)) {
              addListGetAttributeMethod(cls, fieldName, cap);
            } else {
              addGetAttributeMethod(cls, fieldName, cap);
            }
          }
          if (gm.getListAttribute()) {
            addGetListAttributeMethod(cls, fieldName, cap);
          }
          if (gm.getElementSize()) {
            addGetElementSizeMethod(cls, fieldName, cap, field);
          }
          if (gm.selectByText()) {
            addSelectByTextMethod(cls, fieldName, cap);
          }
          if (gm.selectByIndex()) {
            addSelectByIndexMethod(cls, fieldName, cap);
          }
          if (gm.scroll()) {
          }

          if (gm.scroll()) {
            if (isListWebElement(field)) {
              addListScrollMethod(cls, fieldName, cap);
            } else {
              addScrollMethod(cls, fieldName, cap);
            }
          }

          if (gm.switchFrame()) {
            addSwitchFrameMethod(cls, fieldName, cap);
          }
          if (gm.waitForVisibility()) {
            if (isListWebElement(field)) {
              addListWaitForVisibilityMethod(cls, fieldName, cap);
            } else {
              addWaitForVisibilityMethod(cls, fieldName, cap);
            }
          }

          if (gm.waitForInVisibility()) {
            if (isListWebElement(field)) {
              addListWaitForInVisibilityMethod(cls, fieldName, cap);
            } else {
              addWaitForInVisibilityMethod(cls, fieldName, cap);
            }
          }
          if (gm.uploadFile()) {
            addUploadFileMethod(cls, fieldName, cap);
          }
          if (gm.getTextFirstSelectedOption()) {
            addGetTextFirstSelectedOptionMethod(cls, fieldName, cap);
          }

          if (gm.scrollCenterOfElement()) {
            if (isListWebElement(field)) {
              addListScrollCenterOfElementMethod(cls, fieldName, cap);
            } else {
              addScrollCenterOfElementMethod(cls, fieldName, cap);
            }
          }

          if (gm.visualRegression().enabled()) {
            addCheckVisualRegressionMethod(cls, fieldName, cap, gm);
          }

          if (isListWebElement(field)) {
            addListGetAttributeTypeMethods(cls, fieldName, cap, gm);
          } else {
            addGetAttributeTypeMethods(cls, fieldName, cap, gm);
          }

          if (isListWebElement(field)) {
            addListWaitVisibleAttributeMethods(cls, fieldName, cap, gm);
          } else {
            addWaitVisibleAttributeMethods(cls, fieldName, cap, gm);
          }

          if (isListWebElement(field)) {
            addListWaitInVisibleAttributeMethods(cls, fieldName, cap, gm);
          } else {
            addWaitInVisibleAttributeMethods(cls, fieldName, cap, gm);
          }

          addGetListAttributeTypeMethods(cls, fieldName, cap, gm);

        });
  }

  private void addPageAnnotationIfAbsent(JCTree.JCClassDecl cls) {
    boolean exists = cls.mods.annotations.stream()
        .anyMatch(
            a -> a.annotationType.toString().equals("com.builder.annotations.component.Page"));
    if (exists) {
      return;
    }

    var pageAnno = maker.Annotation(memberAccess("com.builder.annotations.component.Page"),
        List.nil());
    cls.mods.annotations = cls.mods.annotations.append(pageAnno);
  }

  private void addClickMethod(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

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

  private void addListClickMethod(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm, VariableElement fieldElement) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // List<WebElement> tipini kontrol et
    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return; // Sadece List<WebElement> için çalışır
    }

    Optional<String> returnPageFqn = getReturnPageFqn(gm);

    // Index parametresi
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"), intType, null);

    if (returnPageFqn.isPresent()) {
      // Return type belirtilmişse o sayfayı döner
      var returnType = memberAccess(returnPageFqn.get());

      // field.get(index) ile elementi al
      var getElement = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
          List.of(maker.Ident(names.fromString("index"))));

      // browser.click(field.get(index))
      var callClick = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("click")),
              List.of(getElement)));

      // context.getBean(returnType.class)
      var getBeanCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
          List.of(maker.Select(returnType, names._class)));

      var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(indexParam),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    } else {
      // Return type belirtilmemişse current page'i döner
      var returnType = maker.Ident(cls.name);

      // field.get(index) ile elementi al
      var getElement = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
          List.of(maker.Ident(names.fromString("index"))));

      // browser.click(field.get(index))
      var callClick = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("click")),
              List.of(getElement)));

      var body = maker.Block(0, List.of(callClick, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(indexParam),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  // List<WebElement> tipini kontrol etmek için yardımcı method
  private boolean isListWebElement(VariableElement fieldElement) {
    if (fieldElement == null) {
      return false;
    }

    String fieldType = fieldElement.asType().toString();
    return fieldType.contains("java.util.List<org.openqa.selenium.WebElement>") ||
        fieldType.contains("List<WebElement>");
  }

  private void addJexeClickMethod(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm, VariableElement fieldElement) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    Optional<String> returnPageFqn = getReturnPageFqn(gm);

    // Field tipini kontrol et - List<WebElement> mi yoksa tek WebElement mi?
    boolean isListField = isListWebElement(fieldElement);

    if (returnPageFqn.isPresent()) {
      var returnType = memberAccess(returnPageFqn.get());

      JCTree.JCStatement callClick;
      List<JCTree.JCVariableDecl> parameters;

      if (isListField) {
        // List<WebElement> için index parametresi ekle
        var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
        var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
            intType, null);
        parameters = List.of(indexParam);

        // jExecutor.click(field.get(index))
        var getElement = maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
            List.of(maker.Ident(names.fromString("index"))));

        callClick = maker.Exec(
            maker.Apply(List.nil(),
                maker.Select(maker.Ident(names.fromString("jExecutor")), names.fromString("click")),
                List.of(getElement)));
      } else {
        // Tek WebElement için parametre yok: jExecutor.click(field)
        parameters = List.nil();
        callClick = maker.Exec(
            maker.Apply(List.nil(),
                maker.Select(maker.Ident(names.fromString("jExecutor")), names.fromString("click")),
                List.of(maker.Ident(names.fromString(field)))));
      }

      var getBeanCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
          List.of(maker.Select(returnType, names._class)));

      var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          parameters,
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    } else {
      var returnType = maker.Ident(cls.name);

      JCTree.JCStatement callClick;
      List<JCTree.JCVariableDecl> parameters;

      if (isListField) {
        // List<WebElement> için index parametresi ekle
        var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
        var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
            intType, null);
        parameters = List.of(indexParam);

        // jExecutor.click(field.get(index))
        var getElement = maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
            List.of(maker.Ident(names.fromString("index"))));

        callClick = maker.Exec(
            maker.Apply(List.nil(),
                maker.Select(maker.Ident(names.fromString("jExecutor")), names.fromString("click")),
                List.of(getElement)));
      } else {
        // Tek WebElement için parametre yok: jExecutor.click(field)
        parameters = List.nil();
        callClick = maker.Exec(
            maker.Apply(List.nil(),
                maker.Select(maker.Ident(names.fromString("jExecutor")), names.fromString("click")),
                List.of(maker.Ident(names.fromString(field)))));
      }

      var body = maker.Block(0, List.of(callClick, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          parameters,
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addListJexeClickMethod(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm, VariableElement fieldElement) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // List<WebElement> tipini kontrol et
    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return; // Sadece List<WebElement> için çalışır
    }

    Optional<String> returnPageFqn = getReturnPageFqn(gm);

    // Index parametresi - List için zorunlu
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"), intType, null);
    List<JCTree.JCVariableDecl> parameters = List.of(indexParam);

    // field.get(index) ile elementi al
    var getElement = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    // jExecutor.click(field.get(index))
    var callClick = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("jExecutor")), names.fromString("click")),
            List.of(getElement)));

    if (returnPageFqn.isPresent()) {
      // Return type belirtilmişse o sayfayı döner
      var returnType = memberAccess(returnPageFqn.get());

      // context.getBean(returnType.class)
      var getBeanCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
          List.of(maker.Select(returnType, names._class)));

      var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          parameters,
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    } else {
      // Return type belirtilmemişse current page'i döner
      var returnType = maker.Ident(cls.name);

      var body = maker.Block(0, List.of(callClick, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          parameters,
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addGenericReturnJexeClickMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // Create type parameter T extends PageFacility
    var pageFacilityType = memberAccess("com.builder.context.PageFacility");
    var typeParam = maker.TypeParameter(names.fromString("T"), List.of(pageFacilityType));

    // Return type is T
    var returnType = maker.Ident(names.fromString("T"));

    // Parameter: Class<T> page
    var classType = memberAccess("java.lang.Class");
    var parameterizedClassType = maker.TypeApply(classType,
        List.of(maker.Ident(names.fromString("T"))));
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("page"),
        parameterizedClassType, null);

    // Method body: jExecutor.click(field); return appContext.getBean(page);
    var callClick = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("jExecutor")), names.fromString("click")),
            List.of(maker.Ident(names.fromString(field)))));

    var getBeanCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
        List.of(maker.Ident(names.fromString("page"))));

    var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.of(typeParam),
        List.of(param),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGenericReturnClickMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // Create type parameter T extends PageFacility
    var pageFacilityType = memberAccess("com.builder.context.PageFacility");
    var typeParam = maker.TypeParameter(names.fromString("T"), List.of(pageFacilityType));

    // Return type is T
    var returnType = maker.Ident(names.fromString("T"));

    // Parameter: Class<T> page
    var classType = memberAccess("java.lang.Class");
    var parameterizedClassType = maker.TypeApply(classType,
        List.of(maker.Ident(names.fromString("T"))));
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("page"),
        parameterizedClassType, null);

    // Method body: browser.click(field); return context.getBean(page);
    var callClick = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("click")),
            List.of(maker.Ident(names.fromString(field)))));

    var getBeanCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
        List.of(maker.Ident(names.fromString("page"))));

    var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.of(typeParam),
        List.of(param),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addListGenericReturnClickMethod(JCTree.JCClassDecl cls, String field, String cap,
      VariableElement fieldElement) {
    String methodName = "click" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // List<WebElement> tipini kontrol et
    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return; // Sadece List<WebElement> için çalışır
    }

    // Create type parameter T extends PageFacility
    var pageFacilityType = memberAccess("com.builder.context.PageFacility");
    var typeParam = maker.TypeParameter(names.fromString("T"), List.of(pageFacilityType));

    // Return type is T
    var returnType = maker.Ident(names.fromString("T"));

    // Parameters: int index, Class<T> page
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"), intType, null);

    var classType = memberAccess("java.lang.Class");
    var parameterizedClassType = maker.TypeApply(classType,
        List.of(maker.Ident(names.fromString("T"))));
    var pageParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("page"),
        parameterizedClassType, null);

    // field.get(index) ile elementi al
    var getElement = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    // Method body: browser.click(field.get(index)); return context.getBean(page);
    var callClick = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("click")),
            List.of(getElement)));

    var getBeanCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("context")), names.fromString("getBean")),
        List.of(maker.Ident(names.fromString("page"))));

    var body = maker.Block(0, List.of(callClick, maker.Return(getBeanCall)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.of(typeParam),
        List.of(indexParam, pageParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addMouseHoverMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "mouseHover" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    var callMouseHover = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("mouseHover")),
            List.of(maker.Ident(names.fromString(field)))));

    var body = maker.Block(0, List.of(callMouseHover, maker.Return(maker.Ident(names._this))));

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

  private void addListMouseHoverMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "mouseHover" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    // index parametresi ekle
    var indexParam = maker.VarDef(
        maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"),
        maker.TypeIdent(TypeTag.INT),
        null);

    // browser.mouseHover(field.get(index)) çağrısı
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var callMouseHover = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("mouseHover")),
            List.of(getCall)));

    var body = maker.Block(0, List.of(callMouseHover, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam), // parametreler listesi
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addClearMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "clear" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    var callClear = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("clear")),
            List.of(maker.Ident(names.fromString(field)))));

    var body = maker.Block(0, List.of(callClear, maker.Return(maker.Ident(names._this))));

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

  private void addSendKeysMethod(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    String methodName = "sendKeys" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    Optional<String> returnPageFqn = getReturnPageFqn(gm);

    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("text"), stringType,
        null);

    if (returnPageFqn.isPresent()) {
      var returnType = memberAccess(returnPageFqn.get());

      var callSendKeys = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(maker.Ident(names.fromString("browser")), names.fromString("sendKeys")),
              List.of(maker.Ident(names.fromString(field)),
                  maker.Ident(names.fromString("text")))));

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
              List.of(maker.Ident(names.fromString(field)),
                  maker.Ident(names.fromString("text")))));

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
    if (hasMethod(cls, methodName)) {
      return;
    }

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

  private void addListIsDisplayedMethod(JCTree.JCClassDecl cls, String field, String cap,
      VariableElement fieldElement) {
    String methodName = "isDisplayed" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // List<WebElement> tipini kontrol et
    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return; // Sadece List<WebElement> için çalışır
    }

    // Index parametresi
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"), intType, null);

    var returnType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.BOOLEAN);

    // field.get(index) ile elementi al
    var getElement = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    // browser.isDisplayed(field.get(index))
    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")), names.fromString("isDisplayed")),
        List.of(getElement));

    var body = maker.Block(0, List.of(maker.Return(call)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addIsDisplayedTimeoutMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "isDisplayed" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.BOOLEAN);
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("timeout"), intType,
        null);

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")), names.fromString("isDisplayed")),
        List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("timeout"))));

    var body = maker.Block(0, List.of(maker.Return(call)));

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

  private void addListIsDisplayedTimeoutMethod(JCTree.JCClassDecl cls, String field, String cap,
      VariableElement fieldElement) {
    String methodName = "isDisplayed" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // List<WebElement> tipini kontrol et
    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return; // Sadece List<WebElement> için çalışır
    }

    var returnType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.BOOLEAN);
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);

    // Index ve timeout parametreleri
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"), intType, null);
    var timeoutParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("timeout"), intType, null);

    // field.get(index) ile elementi al
    var getElement = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    // browser.isDisplayed(field.get(index), timeout)
    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")), names.fromString("isDisplayed")),
        List.of(getElement, maker.Ident(names.fromString("timeout"))));

    var body = maker.Block(0, List.of(maker.Return(call)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam, timeoutParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetTextMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "getText" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

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

  private void addListGetTextMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "getText" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = memberAccess("java.lang.String");

    // index parametresi ekle
    var indexParam = maker.VarDef(
        maker.Modifiers(Flags.PARAMETER),
        names.fromString("index"),
        maker.TypeIdent(TypeTag.INT),
        null);

    // browser.getText(field.get(index)) çağrısı
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")), names.fromString("getText")),
        List.of(getCall));

    var body = maker.Block(0, List.of(maker.Return(call)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam), // parametreler listesi
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetListTextMethod(JCTree.JCClassDecl cls, String field, String cap,
      VariableElement fieldElement) {
    String methodName = "getListText" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return;
    }

    // Return type: List<String>
    var listType = memberAccess("java.util.List");
    var stringType = memberAccess("java.lang.String");
    var parameterizedListType = maker.TypeApply(listType, List.of(stringType));

    // Generate: field.stream().map(browser::getText).toList()
    // Step 1: field.stream()
    var streamCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("stream")),
        List.nil());

    // Step 2: Create method reference browser::getText
    var browserIdent = maker.Ident(names.fromString("browser"));
    var methodRef = maker.Reference(JCTree.JCMemberReference.ReferenceMode.INVOKE,
        names.fromString("getText"), browserIdent, List.nil());

    // Step 3: .map(browser::getText)
    var mapCall = maker.Apply(List.nil(),
        maker.Select(streamCall, names.fromString("map")),
        List.of(methodRef));

    // Step 4: .toList()
    var toListCall = maker.Apply(List.nil(),
        maker.Select(mapCall, names.fromString("toList")),
        List.nil());

    var body = maker.Block(0, List.of(maker.Return(toListCall)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        parameterizedListType,
        List.nil(),
        List.nil(),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetAttributeMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "getAttribute" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = memberAccess("java.lang.String");
    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("attributeName"),
        stringType, null);

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")),
            names.fromString("getElementAttribute")),
        List.of(maker.Ident(names.fromString(field)),
            maker.Ident(names.fromString("attributeName"))));

    var body = maker.Block(0, List.of(maker.Return(call)));

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

  private void addListGetAttributeMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "getAttribute" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = memberAccess("java.lang.String");
    var stringType = memberAccess("java.lang.String");
    var intType = maker.TypeIdent(TypeTag.INT);

    // Parameters: int index, String attributeName
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
        intType, null);
    var attributeParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER),
        names.fromString("attributeName"),
        stringType, null);

    // Method call: browser.getElementAttribute(field.get(index), attributeName)
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)),
            names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")),
            names.fromString("getElementAttribute")),
        List.of(getCall,
            maker.Ident(names.fromString("attributeName"))));

    var body = maker.Block(0, List.of(maker.Return(call)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam, attributeParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetListAttributeMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "getListAttribute" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    // Return type: List<String>
    var listType = memberAccess("java.util.List");
    var stringType = memberAccess("java.lang.String");
    var parameterizedListType = maker.TypeApply(listType, List.of(stringType));

    // Parameter: String attributeName
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("attributeName"),
        stringType, null);

    // Generate: field.stream().map(element -> browser.getElementAttribute(element, attributeName)).toList()
    // Step 1: field.stream()
    var streamCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("stream")),
        List.nil());

    // Step 2: Create lambda: element -> browser.getElementAttribute(element, attributeName)
    var elementParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("element"),
        memberAccess("org.openqa.selenium.WebElement"), null);

    var lambdaBody = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")),
            names.fromString("getElementAttribute")),
        List.of(maker.Ident(names.fromString("element")),
            maker.Ident(names.fromString("attributeName"))));

    var lambda = maker.Lambda(List.of(elementParam), lambdaBody);

    // Step 3: .map(lambda)
    var mapCall = maker.Apply(List.nil(),
        maker.Select(streamCall, names.fromString("map")),
        List.of(lambda));

    // Step 4: .toList()
    var toListCall = maker.Apply(List.nil(),
        maker.Select(mapCall, names.fromString("toList")),
        List.nil());

    var body = maker.Block(0, List.of(maker.Return(toListCall)));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        parameterizedListType,
        List.nil(),
        List.of(param),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetElementSizeMethod(JCTree.JCClassDecl cls, String field, String cap,
      VariableElement fieldElement) {
    String methodName = "getSize" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    boolean isListField = isListWebElement(fieldElement);
    if (!isListField) {
      return;
    }

    var returnType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);

    var call = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)), names.fromString("size")),
        List.nil());

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
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("text"), stringType,
        null);

    var callSelectByText = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("selectByText")),
            List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("text")))));

    var body = maker.Block(0, List.of(callSelectByText, maker.Return(maker.Ident(names._this))));

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
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"), intType,
        null);

    var callSelectByIndex = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("selectByIndex")),
            List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("index")))));

    var body = maker.Block(0, List.of(callSelectByIndex, maker.Return(maker.Ident(names._this))));

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

  private void addScrollMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "scroll" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    var callScroll = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("scrollToElement")),
            List.of(maker.Ident(names.fromString(field)))));

    var body = maker.Block(0, List.of(callScroll, maker.Return(maker.Ident(names._this))));

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

  private void addListScrollMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "scroll" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(TypeTag.INT);

    // Parameter: int index
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
        intType, null);

    // Method call: field.get(index)
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)),
            names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var callScroll = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("scrollToElement")),
            List.of(getCall)));

    var body = maker.Block(0, List.of(callScroll, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addSwitchFrameMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "switchFrame" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    var callSwitchFrame = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")), names.fromString("frame")),
            List.of(maker.Ident(names.fromString(field)))));

    var body = maker.Block(0, List.of(callSwitchFrame, maker.Return(maker.Ident(names._this))));

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

  private void addWaitForVisibilityMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "waitForVisibility" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(com.sun.tools.javac.code.TypeTag.INT);
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("timeout"), intType,
        null);

    var callWaitForVisibility = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("waitForVisibilityOfElement")),
            List.of(maker.Ident(names.fromString(field)),
                maker.Ident(names.fromString("timeout")))));

    var body = maker.Block(0,
        List.of(callWaitForVisibility, maker.Return(maker.Ident(names._this))));

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

  private void addListWaitForVisibilityMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "waitForVisibility" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(TypeTag.INT);

    // Parameters: int index, int timeout
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
        intType, null);
    var timeoutParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("timeout"),
        intType, null);

    // Method call: field.get(index)
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)),
            names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var callWaitForVisibility = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("waitForVisibilityOfElement")),
            List.of(getCall,
                maker.Ident(names.fromString("timeout")))));

    var body = maker.Block(0,
        List.of(callWaitForVisibility, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam, timeoutParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addWaitForInVisibilityMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "waitForInVisibility" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    var callWaitForInVisibility = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("waitUntilInvisibilityOfElement")),
            List.of(maker.Ident(names.fromString(field)))));

    var body = maker.Block(0,
        List.of(callWaitForInVisibility, maker.Return(maker.Ident(names._this))));

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

  private void addListWaitForInVisibilityMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "waitForInVisibility" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(TypeTag.INT);

    // Parameter: int index
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
        intType, null);

    // Method call: field.get(index)
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)),
            names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var callWaitForInVisibility = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("waitUntilInvisibilityOfElement")),
            List.of(getCall)));

    var body = maker.Block(0,
        List.of(callWaitForInVisibility, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }

  private void addGetListAttributeTypeMethods(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.getListAttributeType();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String methodNameSuffix = getAnnotationTypeMethodName(attributeKey);
      String methodName = "getListAttribute" + methodNameSuffix + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      // Return type: List<String>
      var listType = memberAccess("java.util.List");
      var stringType = memberAccess("java.lang.String");
      var parameterizedListType = maker.TypeApply(listType, List.of(stringType));

      // Create string literal for attribute key
      var attributeKeyLiteral = maker.Literal(attributeKey);

      // Generate: field.stream().map(element -> browser.getElementAttribute(element, "attributeKey")).toList()
      // Step 1: field.stream()
      var streamCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString(field)), names.fromString("stream")),
          List.nil());

      // Step 2: Create lambda: element -> browser.getElementAttribute(element, "attributeKey")
      var elementParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("element"),
          memberAccess("org.openqa.selenium.WebElement"), null);

      var lambdaBody = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("browser")),
              names.fromString("getElementAttribute")),
          List.of(maker.Ident(names.fromString("element")), attributeKeyLiteral));

      var lambda = maker.Lambda(List.of(elementParam), lambdaBody);

      // Step 3: .map(lambda)
      var mapCall = maker.Apply(List.nil(),
          maker.Select(streamCall, names.fromString("map")),
          List.of(lambda));

      // Step 4: .toList()
      var toListCall = maker.Apply(List.nil(),
          maker.Select(mapCall, names.fromString("toList")),
          List.nil());

      var body = maker.Block(0, List.of(maker.Return(toListCall)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          parameterizedListType,
          List.nil(),
          List.nil(),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addGetAttributeTypeMethods(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.getAttributeType();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String methodNameSuffix = getAnnotationTypeMethodName(attributeKey);
      String methodName = "getAttribute" + methodNameSuffix + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      var returnType = memberAccess("java.lang.String");

      // Create string literal for attribute key
      var attributeKeyLiteral = maker.Literal(attributeKey);

      var call = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("browser")),
              names.fromString("getElementAttribute")),
          List.of(maker.Ident(names.fromString(field)), attributeKeyLiteral));

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
  }

  private void addListGetAttributeTypeMethods(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.getAttributeType();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String methodNameSuffix = getAnnotationTypeMethodName(attributeKey);
      String methodName = "getAttribute" + methodNameSuffix + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      var returnType = memberAccess("java.lang.String");
      var intType = maker.TypeIdent(TypeTag.INT);

      // Parameter: int index
      var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
          intType, null);

      // Create string literal for attribute key
      var attributeKeyLiteral = maker.Literal(attributeKey);

      // Method call: browser.getElementAttribute(field.get(index), attributeKey)
      var getCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString(field)),
              names.fromString("get")),
          List.of(maker.Ident(names.fromString("index"))));

      var call = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString("browser")),
              names.fromString("getElementAttribute")),
          List.of(getCall, attributeKeyLiteral));

      var body = maker.Block(0, List.of(maker.Return(call)));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(indexParam),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addWaitVisibleAttributeMethods(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.waitVisibleAttribute();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      System.out.println(attributeType);
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String attributeValue = getAttributeTypeValue(attributeType);
      String methodName = "waitVisibleAttribute" + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      var returnType = maker.Ident(cls.name);

      // Create string literals for attribute key and value
      var attributeKeyLiteral = maker.Literal(attributeKey);
      var attributeValueLiteral = maker.Literal(attributeValue);

      var callWaitVisibleAttribute = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(memberAccess("com.sahibinden.core.utils.WaitUtil"),
                  names.fromString("waitVisibleAttribute")),
              List.of(maker.Ident(names.fromString(field)), attributeKeyLiteral,
                  attributeValueLiteral)));

      var body = maker.Block(0,
          List.of(callWaitVisibleAttribute, maker.Return(maker.Ident(names._this))));

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

  private void addListWaitVisibleAttributeMethods(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.waitVisibleAttribute();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      System.out.println(attributeType);
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String attributeValue = getAttributeTypeValue(attributeType);
      String methodName = "waitVisibleAttribute" + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      var returnType = maker.Ident(cls.name);
      var intType = maker.TypeIdent(TypeTag.INT);

      // Parameter: int index
      var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
          intType, null);

      // Create string literals for attribute key and value
      var attributeKeyLiteral = maker.Literal(attributeKey);
      var attributeValueLiteral = maker.Literal(attributeValue);

      // Method call: field.get(index)
      var getCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString(field)),
              names.fromString("get")),
          List.of(maker.Ident(names.fromString("index"))));

      var callWaitVisibleAttribute = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(memberAccess("com.sahibinden.core.utils.WaitUtil"),
                  names.fromString("waitVisibleAttribute")),
              List.of(getCall, attributeKeyLiteral,
                  attributeValueLiteral)));

      var body = maker.Block(0,
          List.of(callWaitVisibleAttribute, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(indexParam),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private void addWaitInVisibleAttributeMethods(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.waitInVisibleAttribute();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String attributeValue = getAttributeTypeValue(attributeType);
      String methodName = "waitInVisibleAttribute" + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      var returnType = maker.Ident(cls.name);

      // Create string literals for attribute key and value
      var attributeKeyLiteral = maker.Literal(attributeKey);
      var attributeValueLiteral = maker.Literal(attributeValue);

      var callWaitInVisibleAttribute = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(memberAccess("com.sahibinden.core.utils.WaitUtil"),
                  names.fromString("waitInVisibleAttribute")),
              List.of(maker.Ident(names.fromString(field)), attributeKeyLiteral,
                  attributeValueLiteral)));

      var body = maker.Block(0,
          List.of(callWaitInVisibleAttribute, maker.Return(maker.Ident(names._this))));

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

  private void addListWaitInVisibleAttributeMethods(JCTree.JCClassDecl cls, String field,
      String cap,
      GenerateMethods gm) {
    var attributeTypes = gm.waitInVisibleAttribute();
    if (attributeTypes == null) {
      return;
    }

    for (var attributeType : attributeTypes) {
      // Skip NULL attribute type
      if (attributeType.name().equals("NULL")) {
        continue;
      }

      String attributeKey = getAttributeTypeKey(attributeType);
      String attributeValue = getAttributeTypeValue(attributeType);
      String methodName = "waitInVisibleAttribute" + cap;

      if (hasMethod(cls, methodName)) {
        continue;
      }

      var returnType = maker.Ident(cls.name);
      var intType = maker.TypeIdent(TypeTag.INT);

      // Parameter: int index
      var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
          intType, null);

      // Create string literals for attribute key and value
      var attributeKeyLiteral = maker.Literal(attributeKey);
      var attributeValueLiteral = maker.Literal(attributeValue);

      // Method call: field.get(index)
      var getCall = maker.Apply(List.nil(),
          maker.Select(maker.Ident(names.fromString(field)),
              names.fromString("get")),
          List.of(maker.Ident(names.fromString("index"))));

      var callWaitInVisibleAttribute = maker.Exec(
          maker.Apply(List.nil(),
              maker.Select(memberAccess("com.sahibinden.core.utils.WaitUtil"),
                  names.fromString("waitInVisibleAttribute")),
              List.of(getCall, attributeKeyLiteral,
                  attributeValueLiteral)));

      var body = maker.Block(0,
          List.of(callWaitInVisibleAttribute, maker.Return(maker.Ident(names._this))));

      var m = maker.MethodDef(
          maker.Modifiers(Flags.PUBLIC),
          names.fromString(methodName),
          returnType,
          List.nil(),
          List.of(indexParam),
          List.nil(),
          body,
          null);
      cls.defs = cls.defs.append(m);
    }
  }

  private String getAttributeTypeKey(com.builder.constants.AttributeTypes attributeType) {
    // Assuming AttributeTypes enum has a getKey() method or similar
    try {
      // Use reflection to get the key value from the enum
      var method = attributeType.getClass().getMethod("getKey");
      return (String) method.invoke(attributeType);
    } catch (Exception e) {
      // Fallback to enum name if getKey() method doesn't exist
      return attributeType.name().toLowerCase();
    }
  }

  private String getAttributeTypeValue(com.builder.constants.AttributeTypes attributeType) {
    // Assuming AttributeTypes enum has a getValue() method or similar
    try {
      // Use reflection to get the value from the enum
      var method = attributeType.getClass().getMethod("getValue");
      return (String) method.invoke(attributeType);
    } catch (Exception e) {
      // Fallback to empty string if getValue() method doesn't exist
      return "";
    }
  }

  private String getAnnotationTypeMethodName(String attributeKey) {
    // Convert attribute key to proper method name format
    // e.g., "data-test" -> "DataTest", "class" -> "Class"
    if (attributeKey == null || attributeKey.isEmpty()) {
      return "";
    }

    String[] parts = attributeKey.split("-");
    StringBuilder result = new StringBuilder();

    for (String part : parts) {
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)));
        if (part.length() > 1) {
          result.append(part.substring(1));
        }
      }
    }

    return result.toString();
  }

  private void addUploadFileMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "uploadFile" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("path"), stringType,
        null);

    var callUploadFile = maker.Exec(
        maker.Apply(List.nil(),
            maker.Ident(names.fromString("uploadFile")),
            List.of(maker.Ident(names.fromString(field)), maker.Ident(names.fromString("path")))));

    var body = maker.Block(0, List.of(callUploadFile, maker.Return(maker.Ident(names._this))));

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

  private void addGetTextFirstSelectedOptionMethod(JCTree.JCClassDecl cls, String field,
      String cap) {
    String methodName = "getFirstSelectedOptionText" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = memberAccess("java.lang.String");

    // Create: new org.openqa.selenium.support.ui.Select(field)
    var selectType = memberAccess("org.openqa.selenium.support.ui.Select");
    var newSelect = maker.NewClass(null, List.nil(), selectType,
        List.of(maker.Ident(names.fromString(field))), null);

    // Create: .getFirstSelectedOption()
    var getFirstSelectedOption = maker.Apply(List.nil(),
        maker.Select(newSelect, names.fromString("getFirstSelectedOption")),
        List.nil());

    // Create: .getText()
    var getText = maker.Apply(List.nil(),
        maker.Select(getFirstSelectedOption, names.fromString("getText")),
        List.nil());

    var body = maker.Block(0, List.of(maker.Return(getText)));

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

  private void addScrollCenterOfElementMethod(JCTree.JCClassDecl cls, String field, String cap) {
    String methodName = "scrollCenterOfElement" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);

    var callScrollCenterOfElement = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("scrollCenterOfElement")),
            List.of(maker.Ident(names.fromString(field)))));

    var body = maker.Block(0,
        List.of(callScrollCenterOfElement, maker.Return(maker.Ident(names._this))));

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

  private void addListScrollCenterOfElementMethod(JCTree.JCClassDecl cls, String field,
      String cap) {
    String methodName = "scrollCenterOfElement" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var intType = maker.TypeIdent(TypeTag.INT);

    // Parameter: int index
    var indexParam = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("index"),
        intType, null);

    // Method call: field.get(index)
    var getCall = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString(field)),
            names.fromString("get")),
        List.of(maker.Ident(names.fromString("index"))));

    var callScrollCenterOfElement = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("scrollCenterOfElement")),
            List.of(getCall)));

    var body = maker.Block(0,
        List.of(callScrollCenterOfElement, maker.Return(maker.Ident(names._this))));

    var m = maker.MethodDef(
        maker.Modifiers(Flags.PUBLIC),
        names.fromString(methodName),
        returnType,
        List.nil(),
        List.of(indexParam),
        List.nil(),
        body,
        null);
    cls.defs = cls.defs.append(m);
  }


  private void addCheckVisualRegressionMethod(JCTree.JCClassDecl cls, String field, String cap,
      GenerateMethods gm) {
    String methodName = "checkVisualRegression" + cap;
    if (hasMethod(cls, methodName)) {
      return;
    }

    var returnType = maker.Ident(cls.name);
    var stringType = memberAccess("java.lang.String");
    var param = maker.VarDef(maker.Modifiers(Flags.PARAMETER), names.fromString("tagName"),
        stringType, null);

    // Get visual regression configuration
    var visualRegression = gm.visualRegression();
    boolean scrollIntoView = visualRegression.scrollIntoView();
    int diff = visualRegression.diff();

    // browser.waitForPageLoads()
    var waitForPageLoads = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("browser")),
                names.fromString("waitForPageLoads")),
            List.nil()));

    // new com.vrt.VRTParam(tagName, field)
    var vrtParamType = memberAccess("com.vrt.VRTParam");
    var newVRTParam = maker.NewClass(null, List.nil(), vrtParamType,
        List.of(maker.Ident(names.fromString("tagName")), maker.Ident(names.fromString(field))),
        null);

    // browser.getRemoteWebDriver()
    var getRemoteWebDriver = maker.Apply(List.nil(),
        maker.Select(maker.Ident(names.fromString("browser")),
            names.fromString("getRemoteWebDriver")),
        List.nil());

    // vrt.checkVisualRegression(browser.getRemoteWebDriver(), scrollIntoView, diff, new VRTParam(tagName, field))
    var checkVisualRegression = maker.Exec(
        maker.Apply(List.nil(),
            maker.Select(maker.Ident(names.fromString("vrt")),
                names.fromString("checkVisualRegression")),
            List.of(
                getRemoteWebDriver,
                maker.Literal(scrollIntoView),
                maker.Literal(diff),
                newVRTParam
            )));

    var body = maker.Block(0,
        List.of(waitForPageLoads, checkVisualRegression, maker.Return(maker.Ident(names._this))));

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
    java.util.List<? extends TypeMirror> mirrors = PageUtil.getTypeMirrorFromAnnotationValue(
        () -> gm.returnPage());
    if (mirrors == null || mirrors.isEmpty()) {
      return Optional.empty();
    }
    String fqn = mirrors.get(0).toString();
    if (Objects.equals(fqn, Nullable.class.getName())) {
      return Optional.empty();
    }
    return Optional.of(fqn);
  }

  private String capitalize(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }
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