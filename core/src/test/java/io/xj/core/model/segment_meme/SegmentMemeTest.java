// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_meme;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class SegmentMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentMeme()
      .setSegmentId(BigInteger.valueOf(23678L))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new SegmentMeme()
      .setSegmentId(BigInteger.valueOf(23678L))
      .validate();
  }

}
