package com.builder.page;

public interface Actions<P> {

  P go(String url);

  String getCurrentUrl();

  <T extends AbstractPage> T go(String url, Class<T> page);

  P tabChange(int index);

  P sleepSecond(int second);
}
