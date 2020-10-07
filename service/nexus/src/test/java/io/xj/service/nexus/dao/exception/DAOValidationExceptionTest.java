package io.xj.service.nexus.dao.exception;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DAOValidationExceptionTest {
  private DAOValidationException subject;

  @Test
  public void withMessage() {
    subject = new DAOValidationException("Be excellent to each other!");

    assertEquals("Be excellent to each other!", subject.getMessage());
  }

  @Test
  public void withMessageAndException() {
    subject = new DAOValidationException("Be excellent to each other!", new IOException("Party on, dudes!"));

    assertEquals("Be excellent to each other!", subject.getMessage());
    assertEquals(IOException.class, subject.getCause().getClass());
    assertEquals("Party on, dudes!", subject.getCause().getMessage());
  }

  @Test
  public void withException() {
    subject = new DAOValidationException(new IOException("Party on, dudes!"));

    assertEquals("Party on, dudes!", subject.getMessage());
    assertEquals(IOException.class, subject.getCause().getClass());
    assertEquals("Party on, dudes!", subject.getCause().getMessage());
  }

}
