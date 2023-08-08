// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.app;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class AppExceptionTest {
  AppException subject;

  @Test
  public void AppException_withMessage() {
    subject = new AppException("Things went wrong, I tell ya!");

    assertEquals("Things went wrong, I tell ya!", subject.getMessage());
  }

  @Test
  public void AppException_withMessageAndException() {
    subject = new AppException("Things went wrong, I tell ya!", new Exception("Truly they did; read all about it."));

    assertEquals("Things went wrong, I tell ya!", subject.getMessage());
    assertEquals("Truly they did; read all about it.", subject.getCause().getMessage());
  }

}
