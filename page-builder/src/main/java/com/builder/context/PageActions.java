package com.builder.context;

import com.builder.page.AbstractPage;
import com.builder.page.Actions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PageActions<P> extends AbstractPage implements Actions<P> {

  @Override
  public P go(String url) {
    browser.getRemoteWebDriver().get(url);
    browser.waitForPageLoads();
    return (P) this;
  }

  @Override
  public <T extends AbstractPage> T go(String url, Class<T> page) {
    go(url);
    return context.getBean(page);
  }

  @Override
  public String getCurrentUrl() {
    return browser.getCurrentUrl();
  }

  @Override
  public P tabChange(int index) {
    browser.tabChange(index);
    return (P) this;
  }

  @Override
  public P sleepSecond(int second) {
    browser.sleepSecond(second);
    return (P) this;
  }
}
