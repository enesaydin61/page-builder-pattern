package com.builder.errors;

public class ElementFieldNotPrivateException extends RuntimeException {

  public ElementFieldNotPrivateException() {
  }

  /**
   * Constructs an ElementFieldNotPrivateException with the specified detail message.
   *
   * @param message the detail message
   */
  public ElementFieldNotPrivateException(String message) {
    super(message);
  }

}
