package com.builder.driver.browser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Slf4j
public class BrowserImpl implements Browser {

  @Getter
  private RemoteWebDriver remoteWebDriver;

  @Getter
  private List<RemoteWebDriver> remoteWebDrivers = new ArrayList<>();

  private WebDriverWait wait;
  private Actions actions;

  public BrowserImpl(RemoteWebDriver remoteWebDriver) {
    setRemoteWebDriver(remoteWebDriver);
  }

  @Override
  public void setRemoteWebDriver(RemoteWebDriver remoteWebDriver) {
    this.remoteWebDriver = remoteWebDriver;
    this.wait = new WebDriverWait(remoteWebDriver, Duration.ofSeconds(10));
    this.actions = new Actions(remoteWebDriver);
  }

  @Override
  public void get(String url) {
    log.info("Navigating to URL: {}", url);
    remoteWebDriver.get(url);
  }

  @Override
  public String getCurrentUrl() {
    return remoteWebDriver.getCurrentUrl();
  }

  @Override
  public Set<String> getWindowHandles() {
    return remoteWebDriver.getWindowHandles();
  }

  @Override
  public void tabChange(int index) {
    ArrayList<String> tabs = new ArrayList<>(getWindowHandles());
    remoteWebDriver.switchTo().window(tabs.get(index));
  }

  @Override
  public void click(WebElement element) {
    waitUntilElementClickable(element);
    element.click();
  }

  @Override
  public void sendKeys(WebElement webElement, CharSequence... keys) {
    clear(webElement);
    webElement.sendKeys(keys);
  }

  @Override
  public void clear(WebElement webElement) {
    webElement.clear();
  }


  @Override
  public boolean isDisplayed(WebElement element) {
    try {
      return element.isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void waitUntilElementClickable(WebElement element) {
    wait.until(ExpectedConditions.elementToBeClickable(element));
  }

  @Override
  public void waitForPageLoads() {
    wait.until(webDriver -> ((JavascriptExecutor) webDriver)
        .executeScript("return document.readyState").equals("complete"));
  }

  @Override
  public void sleepSecond(Integer second) {
    try {
      Thread.sleep(second * 1000L);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Sleep interrupted", e);
    }
  }

}