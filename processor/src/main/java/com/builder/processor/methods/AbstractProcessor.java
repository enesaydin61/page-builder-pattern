package com.builder.processor.methods;

import com.builder.annotations.GenerateMethods;
import com.builder.util.PageUtil;
import com.sun.tools.javac.tree.JCTree;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class AbstractProcessor {

  protected boolean hasMethod(JCTree.JCClassDecl cls, String methodName) {
    for (JCTree def : cls.defs) {
      if (def instanceof JCTree.JCMethodDecl m && m.getName().toString().equals(methodName)) {
        return true;
      }
    }
    return false;
  }

  protected Optional<String> getReturnPageFqn(GenerateMethods gm) {
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

  protected String capitalize(String name) {
    if (name == null || name.isEmpty()) {
      return name;
    }
    return Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  protected boolean isListWebElement(VariableElement fieldElement) {
    if (fieldElement == null) {
      return false;
    }

    String fieldType = fieldElement.asType().toString();
    return fieldType.contains("java.util.List<org.openqa.selenium.WebElement>") ||
        fieldType.contains("List<WebElement>");
  }
}
