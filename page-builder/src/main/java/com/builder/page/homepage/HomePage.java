package com.builder.page.homepage;

import com.builder.annotations.component.GetPage;
import com.builder.context.PageFacility;
import lombok.Data;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Data
@Lazy
@Component
public class HomePage extends PageFacility<HomePage> {

  @GetPage
  private ShowCaseLayout showCaseLayout;

}
