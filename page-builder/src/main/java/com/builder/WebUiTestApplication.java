package com.builder;

import com.builder.provider.AppProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

@Profile("test")
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
    DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.builder"})
public class WebUiTestApplication implements CommandLineRunner {

  @Autowired
  ApplicationContext applicationContext;

  @Override
  public void run(String... args) {
    AppProvider.setContext(applicationContext);
  }
}