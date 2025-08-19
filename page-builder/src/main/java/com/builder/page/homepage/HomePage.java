package com.builder.page.homepage;

import com.builder.annotations.GenerateMethods;
import com.builder.annotations.PageBuilder;
import com.builder.context.PageActions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@PageBuilder
public class HomePage extends PageActions<HomePage> {

  @GenerateMethods(isDisplayed = true)
  @FindBy(id = "logo")
  private WebElement logo;

  @GenerateMethods(click = true)
  @FindBy(css = "#gender-popup-modal .modal-close")
  private WebElement genderModalClose;

  @GenerateMethods(sendKeys = true)
  @FindBy(css = "#sfx-discovery-search-suggestions input")
  private WebElement searchInput;

  @GenerateMethods(selectByText = true)
  @FindBy(css = "[data-testid='search-icon']")
  private WebElement searchButton;

}
