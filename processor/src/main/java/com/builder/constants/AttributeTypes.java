package com.builder.constants;

public enum AttributeTypes {

  NULL(null),
  SRC("src"),
  ALT("alt"),
  SRCSET("srcset"),
  HREF("href"),
  TITLE("title"),
  ARIA_LABEL("aria-label"),
  DATA_LABEL("data-label"),
  DATA_DETAIL("data-detail"),
  DATA_COLOR("data-color"),
  COLOR("color"),
  DATA_SUPERCODE("data-supercode"),
  STYLE("style"),
  ID("id"),
  ClASS("class"),
  VALUE("value"),
  SELECTED("selected"),
  DATA_CONTENT("data-content"),
  INIT_VAL("init-val"),
  NG_SRC("ng-src"),
  DATA_SRC("data-src"),
  TYPE("type"),
  DATA_NOTIFICATION_COUNT("data-notification-count"),
  DATA_CLICK_CATEGORY("data-click-category"),
  DATA_CLICK_EVENT("data-click-event"),
  CHECKED("checked"),
  DISABLED("disabled"),
  DISABLED_VALUE("disabled value"),
  TEXTCONTENT("textContent"),
  INNERHTML("innerHTML"),
  INNERTEXT("innerText"),
  PLACEHOLDER("placeholder"),
  BACKGROUND_IMAGE("background-image"),
  DATA_IMAGE_PATH("data-image-path"),
  MAXLENGTH("maxlength"),
  DATA_ON("data-on"),
  DATA("data"),
  CONTENT("content"),
  PART_STATUS_ID("part-status-id"),
  DATA_COUNT("data-count"),
  DATA_EXPERTISE_STORE_ID("data-expertise-store-id"),
  DATA_RESPONSE_EXTENSION("data-response-extension"),
  SHARE_EMAIL_URL("share-email-url"),

  /**
   * For Wait In/Visible Attribute
   */
  CLASS_NG_START_ATTRIBUTE("class", "ng-star-inserted"),
  CLASS_NG_EMPTY("class", "ng-empty"),
  CLASS_NG_NOT_EMPTY("class", "ng-not-empty"),
  CLASS_NG_HIDE("class", "ng-hide"),
  STYLE_DISPLAY_BLOCK("style", "display: block;"),
  CLASS_FAVORITE("class", " favorite"),
  CLASS_DIALOG_EFFECT("class", "dialogEffect"),
  CLASS_THREAD_VIEW("class", "thread-view"),
  NG_IF_OPTION_SELECTION("ng-if", "step == 'OPTION_SELECTION'"),
  ACTION_PASSWORD_CHANGE("action", "sifre-degistir"),
  CLASS_HIDDEN("class", "hidden"),
  CLASS_DISABLED("class", "disabled"),
  STYLE_DISPLAY_FLEX("style", "display: flex"),
  STYLE_DISPLAY_NONE("style", "display: none;"),
  CLASS_HIGHLIGHTED("class", "highlighted"),
  CLASS_ACTIVE("class", "active"),
  CLASS_PASSIVE("class", "passive");

  private String key;
  private String value;

  AttributeTypes(String key) {
    this.key = key;
  }

  AttributeTypes(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
