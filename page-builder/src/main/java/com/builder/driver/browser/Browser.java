package com.builder.driver.browser;

import java.util.Set;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public interface Browser {

    RemoteWebDriver getRemoteWebDriver();

    void setRemoteWebDriver(RemoteWebDriver remoteWebDriver);

    void get(String url);

    String getCurrentUrl();

    Set<String> getWindowHandles();

    void tabChange(int index);

    void click(WebElement element);

    void sendKeys(WebElement webElement, CharSequence... keys);

    void clear(WebElement webElement);

    boolean isDisplayed(WebElement element);

    void waitUntilElementClickable(WebElement element);

    void waitForPageLoads();

    void sleepSecond(Integer second);

    // Added for AST-generated methods
    String getText(WebElement element);

    void selectByText(WebElement selectElement, String text);

    void selectByIndex(WebElement selectElement, int index);

    String getElementAttribute(WebElement element, String attributeName);

}