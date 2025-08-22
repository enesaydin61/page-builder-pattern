package com.builder.page.homepage;

import com.builder.annotations.GenerateMethods;
import com.builder.annotations.LayoutBuilder;
import com.builder.context.PageFacility;
import java.util.List;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@LayoutBuilder
public class ShowCaseLayout extends PageFacility<ShowCaseLayout> {

  @FindBy(css = "#gender-popup-modal .modal-close")
  private WebElement genderModalClose;

  @FindBy(css = "#sfx-discovery-search-suggestions input")
  private List<WebElement> searchInput;

  @FindBy(css = "[data-testid='search-icon']")
  private WebElement searchButton;

  @GenerateMethods(scrollCenterOfElement = true)
  @FindBy(id = "aaaa")
  private List<WebElement> aaaa;

}
