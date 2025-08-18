package com.builder;

import com.builder.annotations.component.GetPage;
import com.builder.annotations.test.WebTest;
import com.builder.page.builder.homepage.HomePage;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;

@SpringBootTest(classes = WebUiTestApplication.class)
public class SampleTest {

  @GetPage
  private HomePage homePage;

  @BeforeEach
  public void before() {
  }

  @WebTest
  @Description("""
      """)
  public void test() {
    homePage
        .go("https://www.trendyol.com/")
        .clickGenderModalClose()
        .sendKeysSearchInput(RandomStringUtils.randomAlphabetic(10));

  }

}
