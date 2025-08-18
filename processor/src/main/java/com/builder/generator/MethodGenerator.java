package com.builder.generator;

import com.builder.annotations.GenerateMethods;
import java.io.PrintWriter;
import javax.lang.model.element.Element;

public class MethodGenerator implements Method {

  public MethodGenerator generateMethods(Builder builder) {
    if (builder.annotation.click()) {
      Method.generateClickMethod(builder);
    }

    if (builder.annotation.genericReturnClick()) {
      Method.generateGenericReturnClickMethod(builder);
    }

    if (builder.annotation.sendKeys()) {
      Method.generateSendKeysMethod(builder);
    }

    if (builder.annotation.isDisplayed()) {
      Method.generateIsDisplayedMethod(builder);
    }

    if (builder.annotation.getText()) {
      Method.generateGetTextMethod(builder);
    }

    if (builder.annotation.selectByText()) {
      Method.generateSelectByText(builder);
    }

    if (builder.annotation.selectByIndex()) {
      Method.generateSelectByIndex(builder);
    }
    return this;
  }

  public MethodGenerator generateListMethods(Builder builder) {
    if (builder.annotation.isDisplayed()) {
      Method.List.generateIsDisplayedMethod(builder);
    }

    if (builder.annotation.genericReturnClick()) {
      Method.List.generateGenericReturnClickMethod(builder);
    }

    if (builder.annotation.getElementSize()) {
      Method.List.generateGetElementSizeMethod(builder);
    }

    if (builder.annotation.getText()) {
      Method.List.generateGetTextMethod(builder);
    }

    if (builder.annotation.getListText()) {
      Method.List.generateGetListTextMethod(builder);
    }

    if (builder.annotation.getListAttribute()) {
      Method.List.generateGetListAttributeMethod(builder);
    }
    return this;
  }

  public static class Builder {

    private GenerateMethods annotation;
    private Element element;
    private final PrintWriter writer;
    private String fieldName;
    private String builderClassName;

    public Builder(PrintWriter writer) {
      this.writer = writer;
    }

    public Builder setFieldName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public Builder setBuilderClassName(String builderClassName) {
      this.builderClassName = builderClassName;
      return this;
    }

    public Builder setAnnotation(GenerateMethods annotation) {
      this.annotation = annotation;
      return this;
    }

    public Builder setElement(Element element) {
      this.element = element;
      return this;
    }

    public Element getElement() {
      return element;
    }

    public PrintWriter getWriter() {
      return writer;
    }

    public String getFieldName() {
      return fieldName;
    }

    public String getBuilderClassName() {
      if (builderClassName.contains("PageBuilder")) {
        builderClassName = builderClassName.replace("PageBuilder", "Page");
      }
      return builderClassName;
    }

    public GenerateMethods getAnnotation() {
      return annotation;
    }

    public MethodGenerator build() {

      if (element.asType().toString().equals("org.openqa.selenium.WebElement")) {
        return new MethodGenerator().generateMethods(this);
      }

      return new MethodGenerator().generateListMethods(this);
    }
  }
}
