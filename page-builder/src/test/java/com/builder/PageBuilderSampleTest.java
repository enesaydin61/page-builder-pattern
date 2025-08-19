package com.builder;

import com.builder.annotations.component.GetPage;
import com.builder.annotations.test.WebTest;
import com.builder.page.homepage.HomePage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = WebUiTestApplication.class)
public class PageBuilderSampleTest {

  @GetPage
  private HomePage homePage;

  @WebTest
  public void test() {
    homePage
        .go("https://www.trendyol.com/")
        .clickGenderModalClose()
        .sendKeysSearchInput(RandomStringUtils.randomAlphabetic(10));
  }

}
