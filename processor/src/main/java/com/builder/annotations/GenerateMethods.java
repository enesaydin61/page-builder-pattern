package com.builder.annotations;

import com.builder.constants.AttributeTypes;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nullable;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface GenerateMethods {

  boolean click() default false;

  boolean jexeClick() default false;

  boolean genericReturnJexeClick() default false;

  boolean genericReturnClick() default false;

  boolean mouseHover() default false;

  boolean sendKeys() default false;

  boolean clear() default false;

  boolean isDisplayed() default false;

  boolean isDisplayedTimeout() default false;

  boolean getText() default false;

  boolean getListText() default false;

  boolean getAttribute() default false;

  AttributeTypes[] getAttributeType() default { AttributeTypes.NULL };

  boolean getListAttribute() default false;

  AttributeTypes[] getListAttributeType() default { AttributeTypes.NULL };

  boolean getElementSize() default false;

  boolean selectByText() default false;

  boolean selectByIndex() default false;

  VisualRegression visualRegression() default @VisualRegression();

  boolean scroll() default false;

  boolean switchFrame() default false;

  boolean waitForVisibility() default false;

  AttributeTypes[] waitVisibleAttribute() default { AttributeTypes.NULL };

  AttributeTypes[] waitInVisibleAttribute() default { AttributeTypes.NULL };

  boolean waitForInVisibility() default false;

  boolean uploadFile() default false;

  boolean getTextFirstSelectedOption() default false;

  Class<?> returnPage() default Nullable.class;

  boolean scrollCenterOfElement() default false;
}
