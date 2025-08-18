package com.builder.page.builder.homepage;

import com.builder.annotations.GenerateMethods;
import com.builder.annotations.component.Page;

import com.builder.context.PageActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
* @see com.builder.page.homepage.LoginPageBuilder
**/
@Page
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


  public LoginPage sendKeysUsername(String text) {
    browser.sendKeys(username, text);
    return this;
  }

  public boolean isDisplayedUsername() {
    return browser.isDisplayed(username);
  }

  public LoginPage sendKeysPassword(String text) {
    browser.sendKeys(password, text);
    return this;
  }

  public HomePage clickSubmitButton() {
    browser.click(submitButton);
    return context.getBean(HomePage.class);
  }

}
