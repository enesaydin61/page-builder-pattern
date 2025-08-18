package com.builder.processor;

import com.builder.annotations.GenerateMethods;
import com.builder.errors.DuplicateElementException;
import com.builder.errors.ElementFieldNotPrivateException;
import com.builder.generator.MethodGenerator;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.openqa.selenium.support.FindBy;

@SupportedAnnotationTypes({"com.builder.annotations.PageBuilder"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class PageBuilderProcessor extends AbstractProcessor {

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    annotations
        .forEach(annotation -> roundEnv.getElementsAnnotatedWith(annotation)
            .forEach(element ->
            {
              checkElements(element);
              generatePageClass(element);
            }));
    return true;
  }

  private void generatePageClass(Element element) {
    var className = element.getSimpleName().toString();
    var packageName = element.getEnclosingElement().toString();
    var builderClassName = className.replace("PageBuilder", "Page");
    var builderFullName = packageName + "." + builderClassName;

    List<? extends Element> fields = element.getEnclosedElements().stream()
        .filter(e -> ElementKind.FIELD.equals(e.getKind())).toList();

    var builderPath = builderFullName
        .replace(".page.", ".page.builder.");

    var content = Objects.requireNonNull(loadClassContent((TypeElement) element))
        .replace("@PageBuilder", "@Page")
        .replace("com.builder.page.", "com.builder.page.builder.")
        .replace("import com.builder.annotations.PageBuilder;",
            "import com.builder.annotations.component.Page;\n")
        .replace("PageBuilder", "Page")
        .replace("@Page",
            "/**\n* @see " + packageName + "." + className + "\n**/\n@Page");

    JavaFileObject sourceFile;

    try {
      sourceFile = processingEnv.getFiler().createSourceFile(builderPath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try (PrintWriter classWriter = new PrintWriter(sourceFile.openWriter())) {

      classWriter.println(removeLastChar(content.trim()));

      fields
          .stream()
          .filter(hasAnnotate -> Objects.nonNull(hasAnnotate.getAnnotation(GenerateMethods.class)))
          .forEach(field -> {

            new MethodGenerator.Builder(classWriter)
                .setAnnotation(field.getAnnotation(GenerateMethods.class))
                .setFieldName(field.getSimpleName().toString().substring(0, 1).toUpperCase()
                    + field.getSimpleName().toString().substring(1))
                .setBuilderClassName(builderClassName)
                .setElement(field)
                .build();
          });
      classWriter.println("}");
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static String getSourceFileName(TypeElement typeElement) {
    String qualifiedName = typeElement.getQualifiedName().toString();

    String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    String className = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);

    String sourceFileName = packageName.replace('.', '/') + "/" + className + ".java";

    return sourceFileName;
  }

  private static String getSourceFilePath(TypeElement typeElement) {
    String sourceFileName = getSourceFileName(typeElement);
    return "src/main/java/" + sourceFileName; // Örneğin, proje yapısına göre ayarlayın
  }

  private static String readSourceFile(String filePath) {
    Path path = Paths.get(filePath);
    try {
      return new String(Files.readAllBytes(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String loadClassContent(TypeElement typeElement) {
    try {
      Element enclosingElement = typeElement.getEnclosingElement();
      String packageName = ((PackageElement) enclosingElement).getQualifiedName().toString();
      String className = typeElement.getSimpleName().toString() + ".java";

      final FileObject source = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH,
          packageName,
          className);

      try (Reader reader = source.openReader(true)) {
        final StringBuilder builder = new StringBuilder();
        final char[] buf = new char[1024];
        int read;
        while ((read = reader.read(buf)) != -1) {
          builder.append(buf, 0, read);
        }
        return builder.toString();
      }
    } catch (IOException ex) {
      String sourceFilePath = getSourceFilePath(typeElement);
      String content = readSourceFile(sourceFilePath);
      return content;
    }
  }

  private String removeLastChar(String s) {
    return Optional.ofNullable(s).filter(str -> str.length() != 0)
        .map(str -> str.substring(0, str.length() - 1)).orElse(s);
  }


  private void checkElements(Element element) {
    List<? extends Element> fields = element
        .getEnclosedElements()
        .stream()
        .filter(e -> ElementKind.FIELD.equals(e.getKind())).toList();

    HashMap<String, String> selectorMap = new HashMap<>();

    fields
        .forEach(field ->
        {
          if (field.asType().toString().contains("WebElement")
              && field.getAnnotation(FindBy.class) != null) {
            var findBy = field.getAnnotation(FindBy.class);

            Arrays.asList
                    (
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

    selectorMap
        .forEach((name, selector) ->
        {
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