package com.builder.errors;

public class DuplicateElementException extends RuntimeException {

  public DuplicateElementException() {
  }

  /**
   * Constructs an DuplicateElementException with the specified
   * detail message.
   *
   * @param message the detail message
   */
  public DuplicateElementException(String message) {
    super(message);
  }

}
