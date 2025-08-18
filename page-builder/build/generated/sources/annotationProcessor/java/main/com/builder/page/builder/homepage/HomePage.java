package com.builder.page.builder.homepage;

import com.builder.annotations.GenerateMethods;
import com.builder.annotations.component.Page;

import com.builder.context.PageActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
* @see com.builder.page.homepage.HomePageBuilder
**/
@Page
public class HomePage extends PageActions<HomePage> {

  @GenerateMethods(click = true)
  @FindBy(css = "#gender-popup-modal .modal-close")
  private WebElement genderModalClose;

  @GenerateMethods(sendKeys = true)
  @FindBy(css = "#sfx-discovery-search-suggestions input")
  private WebElement searchInput;


  public HomePage clickGenderModalClose() {
    browser.click(genderModalClose);
    return this;
  }

  public HomePage sendKeysSearchInput(String text) {
    browser.sendKeys(searchInput, text);
    return this;
  }

}
