package io.xj.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkstationExceptionTest {

  @Test
  void testConstructor() {
    WorkstationException actualWorkstationException = new WorkstationException("An error occurred");
    assertEquals("io.xj.gui.WorkstationException: An error occurred", actualWorkstationException.toString());
    assertEquals("An error occurred", actualWorkstationException.getLocalizedMessage());
    assertEquals("An error occurred", actualWorkstationException.getMessage());
    assertNull(actualWorkstationException.getCause());
  }

}
