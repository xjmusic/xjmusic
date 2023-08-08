// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.persistence.exception;

import io.xj.nexus.persistence.ManagerValidationException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ManagerValidationExceptionTest {
  ManagerValidationException subject;

  @Test
  public void withMessage() {
    subject = new ManagerValidationException("Be excellent to each other!");

    assertEquals("Be excellent to each other!", subject.getMessage());
  }

  @Test
  public void withMessageAndException() {
    subject = new ManagerValidationException("Be excellent to each other!", new IOException("Party on, dudes!"));

    assertEquals("Be excellent to each other!", subject.getMessage());
    assertEquals(IOException.class, subject.getCause().getClass());
    assertEquals("Party on, dudes!", subject.getCause().getMessage());
  }

  @Test
  public void withException() {
    subject = new ManagerValidationException(new IOException("Party on, dudes!"));

    assertEquals("Party on, dudes!", subject.getMessage());
    assertNull(subject.getCause());
  }

}
