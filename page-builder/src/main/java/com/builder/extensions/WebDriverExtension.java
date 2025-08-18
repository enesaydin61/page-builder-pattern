package com.builder.extensions;

import com.builder.configuration.BrowserProperties;
import com.builder.context.WebTestContext;
import com.builder.driver.browser.Browser;
import com.builder.driver.browser.BrowserImpl;
import com.builder.provider.AppProvider;
import com.builder.provider.WebTestContextProvider;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

@Slf4j
public class WebDriverExtension implements BeforeEachCallback, AfterEachCallback {

  private static final String WEBDRIVER_KEY = "webdriver";

  @Override
  public void beforeEach(ExtensionContext context) {
    WebTestContext testContext = new WebTestContext();
    log.info("WebDriver initialize...");

    var appContext = AppProvider.getContext();
    BrowserProperties browserProperties = appContext.getBean(BrowserProperties.class);

    WebDriver driver = createWebDriver(browserProperties.getBrowser(),
        browserProperties.isHeadless());
    RemoteWebDriver remoteWebDriver = (RemoteWebDriver) driver;
    Browser browser = new BrowserImpl(remoteWebDriver);
    testContext.setBrowser(browser);
    WebTestContextProvider.set(testContext);

    context.getStore(ExtensionContext.Namespace.GLOBAL).put(WEBDRIVER_KEY, browser);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    log.info("WebDriver kapatılıyor...");

    Browser browser = context.getStore(ExtensionContext.Namespace.GLOBAL)
        .get(WEBDRIVER_KEY, Browser.class);

    if (browser.getRemoteWebDriver() != null) {
      try {
        browser.getRemoteWebDriver().quit();
        log.info("WebDriver closed");
      } catch (Exception e) {
        log.error("WebDriver kapatılırken hata oluştu: {}", e.getMessage());
      }
    }
  }

  private WebDriver createWebDriver(String browserName, boolean headless) {
    WebDriver driver;

    switch (browserName) {
      case "chrome":
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.addArguments("--window-size=1920,1080");

        if (headless) {
          chromeOptions.addArguments("--headless");
        }

        driver = new ChromeDriver(chromeOptions);
        break;

      case "firefox":
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions firefoxOptions = new FirefoxOptions();

        if (headless) {
          firefoxOptions.addArguments("--headless");
        }

        driver = new FirefoxDriver(firefoxOptions);
        break;

      default:
        throw new IllegalArgumentException("Desteklenmeyen browser: " + browserName);
    }

    driver.manage().window().maximize();
    return driver;
  }

  public static WebDriver getWebDriver(ExtensionContext context) {
    return context.getStore(ExtensionContext.Namespace.GLOBAL)
        .get(WEBDRIVER_KEY, WebDriver.class);
  }
} 