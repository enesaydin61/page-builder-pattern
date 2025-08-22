package com.builder.driver.browser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevTools;
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
  public void back() {

  }

  @Override
  public <T> T goToPage(T var1, String var2) {
    return null;
  }

  @Override
  public void goToPage(String var1) {

  }

  @Override
  public void addCookie(Cookie var1) {

  }

  @Override
  public String getCurrentUrl() {
    return remoteWebDriver.getCurrentUrl();
  }

  @Override
  public String getTitle() {
    return "";
  }

  @Override
  public String getPageSource() {
    return "";
  }

  @Override
  public void close() {

  }

  @Override
  public void quit() {

  }

  @Override
  public Set<String> getWindowHandles() {
    return remoteWebDriver.getWindowHandles();
  }

  @Override
  public String getWindowHandle() {
    return "";
  }

  @Override
  public String getText(WebElement var1) {
    return "";
  }

  @Override
  public String getElementAttribute(WebElement var1, String var2) {
    return "";
  }

  @Override
  public void tabChange(int index) {
    ArrayList<String> tabs = new ArrayList<>(getWindowHandles());
    remoteWebDriver.switchTo().window(tabs.get(index));
  }

  @Override
  public void frame(int var1) {

  }

  @Override
  public void frame(WebElement var1) {

  }

  @Override
  public void parentFrame() {

  }

  @Override
  public void defaultContent() {

  }

  @Override
  public String getLocationPathName() {
    return "";
  }

  @Override
  public String getCookieValueByName(String var1) {
    return "";
  }

  @Override
  public void click(WebElement element) {
    waitUntilElementClickable(element);
    element.click();
  }

  @Override
  public void mouseHoverAndClickElement(WebElement var1, WebElement var2) {

  }

  @Override
  public void mouseHoverAndDoubleClickElement(WebElement var1, WebElement var2) {

  }

  @Override
  public void sendKeys(WebElement webElement, CharSequence... keys) {
    clear(webElement);
    webElement.sendKeys(keys);
  }

  @Override
  public void sendKeysWithoutClear(WebElement var1, CharSequence... var2) {

  }

  @Override
  public void clear(WebElement webElement) {
    webElement.clear();
  }

  @Override
  public void waitAndSendKeysSlowly(WebElement var1, String var2) {

  }

  @Override
  public void pageRefresh() {

  }

  @Override
  public DevTools getDevtools() {
    return null;
  }

  @Override
  public boolean isDataLayerObjectContains(String var1) {
    return false;
  }

  @Override
  public void addHeaderToAllRequest(String var1, Object var2) {

  }

  @Override
  public void scrollCenterOfElement(WebElement var1) {

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
  public boolean isDisplayed(WebElement var1, boolean var2) {
    return false;
  }

  @Override
  public boolean isDisplayed(WebElement var1, int var2) {
    return false;
  }

  @Override
  public boolean isDisplayed(WebElement var1, int var2, boolean var3) {
    return false;
  }

  @Override
  public boolean isElementPresent(WebElement var1) {
    return false;
  }

  @Override
  public boolean isElementPresent(WebElement var1, int var2) {
    return false;
  }

  @Override
  public boolean isElementPresent(By var1) {
    return false;
  }

  @Override
  public boolean isElementPresent(By var1, int var2) {
    return false;
  }

  @Override
  public void waitForVisibility(By var1, int var2) {

  }

  @Override
  public void waitForVisibilityOfElement(WebElement var1, int var2) {

  }

  @Override
  public void waitUntilVisibilityOfElement(WebElement var1) {

  }

  @Override
  public void waitUntilInvisibilityOfElement(WebElement var1) {

  }

  @Override
  public void waitUntilInvisibilityOfElement(WebElement var1, int var2) {

  }

  @Override
  public void waitUntilElementClickable(WebElement element) {
    wait.until(ExpectedConditions.elementToBeClickable(element));
  }

  @Override
  public void waitVisibleAttribute(WebElement var1, String var2, String var3) {

  }

  @Override
  public void waitInVisibleAttribute(WebElement var1, String var2, String var3) {

  }

  @Override
  public boolean isTextDisplayedOnWebPage(String var1) {
    return false;
  }

  @Override
  public String resolveBrowserType() {
    return "";
  }

  @Override
  public void waitForPageLoads() {
    wait.until(webDriver -> ((JavascriptExecutor) webDriver)
        .executeScript("return document.readyState").equals("complete"));
  }

  @Override
  public void sleepMin() {

  }

  @Override
  public void sleepMin(Integer var1) {

  }

  @Override
  public void sleepSecond() {

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

  @Override
  public void sleepMillis(Integer var1) {

  }

  @Override
  public void mouseHover(WebElement var1) {

  }

  @Override
  public void moveToElement(WebElement var1) {

  }

  @Override
  public void doubleClickAction(WebElement var1) {

  }

  @Override
  public void dragAndDropElementToElement(WebElement var1, WebElement var2) {

  }

  @Override
  public void scrollToTop() {

  }

  @Override
  public void scrollToBottom() {

  }

  @Override
  public void scrollToBottom(int var1) {

  }

  @Override
  public void focusElement(WebElement var1) {

  }

  @Override
  public void selectByText(WebElement var1, String var2) {

  }

  @Override
  public void selectByIndex(WebElement var1, int var2) {

  }

  @Override
  public void selectByValue(WebElement var1, String var2) {

  }

  @Override
  public void scrollToElement(WebElement var1) {

  }


}