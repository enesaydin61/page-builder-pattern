package com.builder.page.homepage;

import com.builder.annotations.GenerateMethods;
import com.builder.annotations.PageBuilder;
import com.builder.context.PageActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@PageBuilder
public class LoginPage extends PageActions<LoginPage> {

  @FindBy(id = "username")
  @GenerateMethods(sendKeys = true, isDisplayed = true)
  private WebElement username;

  @FindBy(id = "password")
  @GenerateMethods(sendKeys = true)
  private WebElement password;

  @FindBy(css = ".submit-btn")
  @GenerateMethods(click = true, returnPage = HomePage.class)
  private WebElement submitButton;

}
