//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.segment.sub.Choice;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

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
      .setProgramId(BigInteger.valueOf(1))
      .setSegmentId(BigInteger.valueOf(10))
      .setSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_okWithTypeEnum() throws CoreException {
    subject
      .setTypeEnum(ProgramType.Main)
      .setProgramId(BigInteger.valueOf(1))
      .setSegmentId(BigInteger.valueOf(10))
      .setSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_okWithoutTranspose() throws CoreException {
    subject
      .setType("Main")
      .setProgramId(BigInteger.valueOf(1))
      .setSegmentId(BigInteger.valueOf(10))
      .setSequenceBindingId(UUID.randomUUID())
      .validate();
  }


  @Test
  public void validate_exceptionWithoutSegmentId() throws CoreException {
    failure.expect(CoreException.class);
    failure.expectMessage("Segment ID is required");

    subject
      .setType("Main")
      .setProgramId(BigInteger.valueOf(1))
      .setSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void validate_exceptionWithoutProgramId() throws CoreException {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    subject
      .setType("Main")
      .setSegmentId(BigInteger.valueOf(1))
      .setSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2)
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type", "transpose"), new Choice().getResourceAttributeNames());
  }

}
