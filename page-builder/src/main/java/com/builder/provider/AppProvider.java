package com.builder.provider;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AppProvider implements ApplicationContextAware {

  @Setter
  @Getter
  private static ApplicationContext context;

  public void setApplicationContext(ApplicationContext ctx) {
    context = ctx;
  }
}