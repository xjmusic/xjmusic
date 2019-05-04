//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.error;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorsWrapperTest {
  ErrorsWrapper subject;

  @Before
  public void setUp() throws Exception {
    subject = new ErrorsWrapper();
  }

  @Test
  public void setErrors_getErrors() {
    subject.setErrors(ImmutableList.of(new Error("One"), new Error("Two")));

    assertEquals(2, subject.getErrors().size());
  }

  @Test
  public void setErrorsImmutably_thenAddAnyway() {
    subject.setErrors(ImmutableList.of(new Error("One"), new Error("Two")));

    subject.add(new Error("Three"));

    assertEquals(3, subject.getErrors().size());
  }

  @Test
  public void add() {
    subject.add(new Error("One"));
    subject.add(new Error("Two"));

    assertEquals(2, subject.getErrors().size());
  }
}
