package com.builder.generator;

import com.builder.util.PageUtil;
import java.util.Optional;
import javax.annotation.Nullable;

@SuppressWarnings("ResultOfMethodCallIgnored")
public interface Method {

  String CLASS_EXTENSION = ".class";
      
  static void generateClickMethod(MethodGenerator.Builder builder) {
    var returnPageName = PageUtil.getClassSimpleName(() -> builder.getAnnotation().returnPage());
    Optional
        .of(returnPageName)
        .filter(isNullable -> !isNullable.equals(Nullable.class.getSimpleName()))
        .ifPresentOrElse(
            page -> {
              String _className = builder.getBuilderClassName();
              builder.setBuilderClassName(page);
              builder.getWriter().println("""
                    public %s click%s() {
                      browser.click(%s);
                      return context.getBean(%s);
                    }
                  """.formatted(builder.getBuilderClassName(), builder.getFieldName(),
                  builder.getElement().getSimpleName(),
                  builder.getBuilderClassName().concat(CLASS_EXTENSION)));
              builder.setBuilderClassName(_className);
            },
            () -> {
              builder.getWriter().println("""
                    public %s click%s() {
                      browser.click(%s);
                      return this;
                    }
                  """.formatted(builder.getBuilderClassName(), builder.getFieldName(),
                  builder.getElement().getSimpleName()));
            });
  }

  static void generateGenericReturnClickMethod(MethodGenerator.Builder builder) {
    builder.getWriter().println("""
          public <T extends PageFacility> T click%s(Class<T> page) {
            browser.click(%s);
            return context.getBean(page);
          }
        """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
  }

  static void generateGetTextMethod(MethodGenerator.Builder builder) {
    builder.getWriter().println("""
          public String getText%s() {
            return browser.getText(%s);
          }
        """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
  }

  static void generateIsDisplayedMethod(MethodGenerator.Builder builder) {
    builder.getWriter().println("""
          public boolean isDisplayed%s() {
            return browser.isDisplayed(%s);
          }
        """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
  }

  static void generateSendKeysMethod(MethodGenerator.Builder builder) {
    var returnPageName = PageUtil.getClassSimpleName(() -> builder.getAnnotation().returnPage());
    Optional
        .of(returnPageName)
        .filter(isNullable -> !isNullable.equals(Nullable.class.getSimpleName()))
        .ifPresentOrElse(
            page -> {
              String _className = builder.getBuilderClassName();
              builder.setBuilderClassName(page);
              builder.getWriter().println("""
                    public %s sendKeys%s(String text) {
                      browser.sendKeys(%s, text);
                      return context.getBean(%s);
                    }
                  """.formatted(builder.getBuilderClassName(), builder.getFieldName(),
                  builder.getElement().getSimpleName(),
                  builder.getBuilderClassName().concat(CLASS_EXTENSION)));
              builder.setBuilderClassName(_className);
            },
            () -> {
              builder.getWriter().println("""
                    public %s sendKeys%s(String text) {
                      browser.sendKeys(%s, text);
                      return this;
                    }
                  """.formatted(builder.getBuilderClassName(), builder.getFieldName(),
                  builder.getElement().getSimpleName()));
            });
  }

  static void generateSelectByText(MethodGenerator.Builder builder) {
    builder.getWriter().println("""
          public %s selectByText%s(String text) {
            browser.selectByText(%s, text);
            return this;
          }
        """.formatted(builder.getBuilderClassName(), builder.getFieldName(),
        builder.getElement().getSimpleName()));
  }

  static void generateSelectByIndex(MethodGenerator.Builder builder) {
    builder.getWriter().println("""
          public %s selectByIndex%s(int index) {
            browser.selectByIndex(%s, index);
            return this;
          }
        """.formatted(builder.getBuilderClassName(), builder.getFieldName(),
        builder.getElement().getSimpleName()));
  }

  interface List {

    static void generateGenericReturnClickMethod(MethodGenerator.Builder builder) {
      builder.getWriter().println("""
            public <T extends PageFacility> T click%s(int index, Class<T> page) {
              browser.click(%s.get(index));
              return context.getBean(page);
            }
          """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
    }

    static void generateGetElementSizeMethod(MethodGenerator.Builder builder) {
      builder.getWriter().println("""
            public int getSize%s() {
              return %s.size();
            }
          """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
    }

    static void generateIsDisplayedMethod(MethodGenerator.Builder builder) {
      builder.getWriter().println("""
            public boolean isDisplayed%s(int index) {
              return browser.isDisplayed(%s.get(index));
            }
          """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
    }

    static void generateGetTextMethod(MethodGenerator.Builder builder) {
      builder.getWriter().println("""
            public String getText%s(int index) {
              return browser.getText(%s.get(index));
            }
          """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
    }

    static void generateGetListTextMethod(MethodGenerator.Builder builder) {
      builder.getWriter().println("""
            public List<String> getListText%s() {
              return %s.stream().map(browser::getText).toList();
            }
          """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
    }

    static void generateGetListAttributeMethod(MethodGenerator.Builder builder) {
      builder.getWriter().println("""
            public List<String> getListAttribute%s(String attributeName) {
          return %s.stream().map(element -> browser.getElementAttribute(element, attributeName)).toList();
            }
          """.formatted(builder.getFieldName(), builder.getElement().getSimpleName()));
    }

  }
}
