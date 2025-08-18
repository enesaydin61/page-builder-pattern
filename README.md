# Page Builder Pattern

Bu proje, **Annotation Processing** kullanarak Page Object Pattern'i otomatik olarak generate eden bir Java Web UI test framework'üdür. Selenium WebDriver ve Spring Boot teknolojileri ile geliştirilmiştir.

## 🎯 Proje Amacı

Bu framework, web uygulamaları için test otomasyonu yazımını kolaylaştırmak ve Page Object Pattern'i daha etkin kullanmak amacıyla geliştirilmiştir. Annotation Processing sayesinde, manuel olarak builder sınıfları yazmak yerine otomatik olarak generate edilir.

## 🏗️ Proje Yapısı

Proje iki ana modülden oluşmaktadır:

```
page_builder/
├── page-builder/     # Ana test framework modülü
├── processor/        # Annotation processor modülü
├── build.gradle.kts  # Root build dosyası
└── settings.gradle.kts
```

### Modüller

- **`page-builder`**: Web test framework'ü, Spring Boot entegrasyonu ve test sınıfları
- **`processor`**: Annotation processor, otomatik kod generate etme

## 🚀 Teknolojiler

- **Java 21**
- **Spring Boot 3.2.0**
- **Selenium WebDriver 4.15.0**
- **JUnit 5**
- **Gradle**

## 📋 Gereksinimler

- Java 21 veya üzeri
- Gradle 8.0+
- Chrome/Firefox tarayıcı (WebDriver için)

## 🔧 Kurulum

1. Projeyi klonlayın:
```bash
git clone <repository-url>
cd page_builder_pattern
```

2. Projeyi build edin:
```bash
./gradlew clean build
```

3. Testleri çalıştırın:
```bash
./gradlew test
```

## 📖 Kullanım

### 1. Page Builder Sınıfı Oluşturma

Öncelikle `@PageBuilder` annotation'ı ile bir builder sınıfı oluşturun:

```java
@PageBuilder
public class HomePageBuilder extends PageActions<HomePageBuilder> {

  @GenerateMethods(isDisplayed = true)
  @FindBy(id = "logo")
  private WebElement logo;

  @GenerateMethods(click = true)
  @FindBy(css = "#gender-popup-modal .modal-close")
  private WebElement genderModalClose;

  @GenerateMethods(sendKeys = true)
  @FindBy(css = "#sfx-discovery-search-suggestions input")
  private WebElement searchInput;
}
```

### 2. Test Sınıfı Yazma

Test sınıfınızda `@WebTest` annotation'ını kullanın:

```java
@SpringBootTest(classes = WebUiTestApplication.class)
public class PageBuilderSampleTest {

  @GetPage
  private HomePage homePage;

  @WebTest
  public void test() {
    homePage
        .go("https://www.trendyol.com/")
        .clickGenderModalClose()
        .sendKeysSearchInput("test");
  }
}
```

### 3. Annotation'lar

#### `@PageBuilder`
Page builder sınıflarını işaretler ve otomatik kod generation'ını tetikler.

#### `@GenerateMethods`
WebElement'ler için hangi metodların generate edileceğini belirtir:
- `click = true`: Click metodu oluşturur
- `sendKeys = true`: SendKeys metodu oluşturur  
- `isDisplayed = true`: IsDisplayed metodu oluşturur

#### `@WebTest`
Test metodlarını işaretler ve WebDriver extension'ını aktif eder.

#### `@GetPage`
Page object'leri dependency injection ile inject eder.

## ⚙️ Annotation Processor

Annotation processor, `@PageBuilder` ile işaretlenmiş sınıfları bulur ve:

1. `PageBuilder` sınıf adını `Page` ile değiştirir
2. Package'ı `com.builder.page.builder.*` altına taşır
3. `@GenerateMethods` annotation'larına göre fluent metodlar generate eder
4. Spring `@Page` annotation'ı ekler

### Generate Edilen Kod Örneği

```java
/**
 * @see com.builder.page.homepage.HomePageBuilder
 **/
@Page
public class HomePage extends PageActions<HomePage> {
  
  // Otomatik generate edilen metodlar
  public HomePage clickGenderModalClose() {
    genderModalClose.click();
    return this;
  }
  
  public HomePage sendKeysSearchInput(String text) {
    searchInput.sendKeys(text);
    return this;
  }
  
  public boolean isDisplayedLogo() {
    return logo.isDisplayed();
  }
}
```

## 🎨 Özellikler

- **Otomatik Kod Generation**: Annotation processing ile otomatik builder metodları
- **Fluent Interface**: Method chaining ile okunabilir test kodları
- **Spring Integration**: Dependency injection desteği
- **WebDriver Management**: Otomatik WebDriver yaşam döngüsü yönetimi
- **Page Object Pattern**: Temiz ve sürdürülebilir test kodu yapısı

## 📁 Örnek Test Senaryosu

```java
@SpringBootTest(classes = WebUiTestApplication.class)
public class ECommerceTest {

  @GetPage
  private HomePage homePage;
  
  @GetPage  
  private SearchResultsPage searchResultsPage;

  @WebTest
  public void searchProduct() {
    homePage
        .go("https://example.com")
        .clickGenderModalClose()
        .sendKeysSearchInput("laptop")
        .clickSearchButton();
        
    searchResultsPage
        .verifyResultsDisplayed()
        .clickFirstProduct();
  }
}
```