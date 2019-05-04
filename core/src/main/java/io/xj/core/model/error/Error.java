//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.error;

public class Error {
  public static final String KEY_ONE = "error";
  public static final String KEY_MANY = "errors";
  private String detail;

  /**
   Empty constructor
   */
  public Error() {
  }

  /**
   Construct error with detail message

   @param detail of error
   */
  public Error(String detail) {
    this.detail = detail;
  }

  /**
   Get detail message of this error

   @return message
   */
  public String getDetail() {
    return detail;
  }

  /**
   Set Error detail message

   @param detail message
   @return this error (for chaining setters)
   */
  public Error setDetail(String detail) {
    this.detail = detail;
    return this;
  }
}
