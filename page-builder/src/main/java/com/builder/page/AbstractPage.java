package com.builder.page;

import com.builder.driver.browser.Browser;
import com.builder.provider.AppProvider;
import com.builder.provider.WebTestContextProvider;
import org.openqa.selenium.support.PageFactory;
import org.springframework.context.ApplicationContext;

public class AbstractPage {

  protected final Browser browser;
  protected ApplicationContext context = AppProvider.getContext();

  protected AbstractPage() {
    this.browser = WebTestContextProvider.get().getBrowser();
    PageFactory.initElements(browser.getRemoteWebDriver(), this);
  }

}
