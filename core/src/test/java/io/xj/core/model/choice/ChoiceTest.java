//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.choice;

import io.xj.core.exception.CoreException;
import io.xj.core.model.sequence.SequenceType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class ChoiceTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  Choice subject;

  @Before
  public void setUp() throws Exception {
    subject = new Choice();
  }

  @Test
  public void validate() throws CoreException {
    subject
      .setType("Main")
      .setSegmentId(BigInteger.valueOf(10))
      .setSequencePatternId(BigInteger.valueOf(20))
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_okWithTypeEnum() throws CoreException {
    subject
      .setTypeEnum(SequenceType.Main)
      .setSegmentId(BigInteger.valueOf(10))
      .setSequencePatternId(BigInteger.valueOf(20))
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_okWithoutTranspose() throws CoreException {
    subject
      .setType("Main")
      .setSegmentId(BigInteger.valueOf(10))
      .setSequencePatternId(BigInteger.valueOf(20))
      .validate();
  }


  @Test
  public void validate_exceptionWithoutSegmentId() throws CoreException {
    failure.expect(CoreException.class);
    failure.expectMessage("Segment ID is required");

    subject
      .setType("Main")
      .setSequencePatternId(BigInteger.valueOf(20))
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_exceptionWithoutSequenceOrSequencePatternId() throws CoreException {
    failure.expect(CoreException.class);
    failure.expectMessage("Required to have either");

    subject
      .setType("Main")
      .setSegmentId(BigInteger.valueOf(10))
      .setTranspose(-2)
      .validate();
  }

  // TODO implement test for aggregate()

}
