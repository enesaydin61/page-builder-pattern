package com.builder.page;

import com.builder.driver.browser.Browser;
import com.builder.provider.AppProvider;
import com.builder.provider.WebTestContextProvider;
import com.vrt.VRT;
import executor.JavaScriptExecutor;
import org.openqa.selenium.support.PageFactory;
import org.springframework.context.ApplicationContext;

public class AbstractPage {

  protected final Browser browser;
  protected ApplicationContext context = AppProvider.getContext();
  protected final JavaScriptExecutor jExecutor;
  protected final VRT vrt;

  protected AbstractPage() {
    vrt = context.getBean(VRT.class);
    this.browser = WebTestContextProvider.get().getBrowser();
    this.jExecutor = new JavaScriptExecutor(browser.getRemoteWebDriver());
    PageFactory.initElements(browser.getRemoteWebDriver(), this);
  }

}
