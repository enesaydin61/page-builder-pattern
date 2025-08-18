package com.builder.annotations.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AliasFor;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Lazy
@Autowired
public @interface GetPage {

  @AliasFor(
      annotation = Autowired.class,
      attribute = "required"
  )
  boolean autowiringMode() default true;
}
