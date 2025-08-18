package com.builder.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nullable;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface GenerateMethods {

  boolean click() default false;

  boolean genericReturnClick() default false;

  boolean sendKeys() default false;

  boolean isDisplayed() default false;

  boolean getText() default false;

  boolean getListText() default false;

  boolean getListAttribute() default false;

  boolean getElementSize() default false;

  boolean selectByText() default false;

  boolean selectByIndex() default false;

  Class<?> returnPage() default Nullable.class;

}
