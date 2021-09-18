package io.xj.nexus.service.exception;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ServiceValidationExceptionTest {
  private ServiceValidationException subject;

  @Test
  public void withMessage() {
    subject = new ServiceValidationException("Be excellent to each other!");

    assertEquals("Be excellent to each other!", subject.getMessage());
  }

  @Test
  public void withMessageAndException() {
    subject = new ServiceValidationException("Be excellent to each other!", new IOException("Party on, dudes!"));

    assertEquals("Be excellent to each other!", subject.getMessage());
    assertEquals(IOException.class, subject.getCause().getClass());
    assertEquals("Party on, dudes!", subject.getCause().getMessage());
  }

  @Test
  public void withException() {
    subject = new ServiceValidationException(new IOException("Party on, dudes!"));

    assertEquals("Party on, dudes!", subject.getMessage());
    assertNull(subject.getCause());
  }

}
