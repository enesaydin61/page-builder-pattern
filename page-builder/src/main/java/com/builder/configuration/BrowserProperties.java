package com.builder.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Getter
@Component
public class BrowserProperties {

  @Value("#{systemProperties['is.headless'] ?: false }")
  private boolean isHeadless;

  @Value("#{systemProperties['browser'] ?: 'chrome' }")
  private String browser;

}
