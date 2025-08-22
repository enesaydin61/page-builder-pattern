package com.builder.driver.browser;

import java.util.List;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.remote.RemoteWebDriver;

public interface Browser {

  RemoteWebDriver getRemoteWebDriver();

  void setRemoteWebDriver(RemoteWebDriver var1);

  List<RemoteWebDriver> getRemoteWebDrivers();

  void get(String var1);

  void back();

  <T> T goToPage(T var1, String var2);

  void goToPage(String var1);

  void addCookie(Cookie var1);

  String getCurrentUrl();

  String getTitle();

  String getPageSource();

  void close();

  void quit();

  Set<String> getWindowHandles();

  String getWindowHandle();

  String getText(WebElement var1);

  String getElementAttribute(WebElement var1, String var2);

  boolean isDisplayed(WebElement var1);

  boolean isDisplayed(WebElement var1, boolean var2);

  boolean isDisplayed(WebElement var1, int var2);

  boolean isDisplayed(WebElement var1, int var2, boolean var3);

  boolean isElementPresent(WebElement var1);

  boolean isElementPresent(WebElement var1, int var2);

  boolean isElementPresent(By var1);

  boolean isElementPresent(By var1, int var2);

  void waitForVisibility(By var1, int var2);

  void waitForVisibilityOfElement(WebElement var1, int var2);

  void waitUntilVisibilityOfElement(WebElement var1);

  void waitUntilInvisibilityOfElement(WebElement var1);

  void waitUntilInvisibilityOfElement(WebElement var1, int var2);

  void waitUntilElementClickable(WebElement var1);

  void waitVisibleAttribute(WebElement var1, String var2, String var3);

  void waitInVisibleAttribute(WebElement var1, String var2, String var3);

  boolean isTextDisplayedOnWebPage(String var1);

  String resolveBrowserType();

  void tabChange(int var1);

  void frame(int var1);

  void frame(WebElement var1);

  void parentFrame();

  void defaultContent();

  String getLocationPathName();

  String getCookieValueByName(String var1);

  void waitForPageLoads();

  void sleepMin();

  void sleepMin(Integer var1);

  void sleepSecond();

  void sleepSecond(Integer var1);

  void sleepMillis(Integer var1);

  void click(WebElement var1);

  void mouseHoverAndClickElement(WebElement var1, WebElement var2);

  void mouseHoverAndDoubleClickElement(WebElement var1, WebElement var2);

  void mouseHover(WebElement var1);

  void moveToElement(WebElement var1);

  void doubleClickAction(WebElement var1);

  void dragAndDropElementToElement(WebElement var1, WebElement var2);

  void scrollToTop();

  void scrollToBottom();

  void scrollToBottom(int var1);

  void focusElement(WebElement var1);

  void selectByText(WebElement var1, String var2);

  void selectByIndex(WebElement var1, int var2);

  void selectByValue(WebElement var1, String var2);

  void scrollToElement(WebElement var1);

  void sendKeys(WebElement var1, CharSequence... var2);

  void sendKeysWithoutClear(WebElement var1, CharSequence... var2);

  void clear(WebElement var1);

  void waitAndSendKeysSlowly(WebElement var1, String var2);

  void pageRefresh();

  DevTools getDevtools();

  boolean isDataLayerObjectContains(String var1);

  void addHeaderToAllRequest(String var1, Object var2);

  void scrollCenterOfElement(WebElement var1);
}