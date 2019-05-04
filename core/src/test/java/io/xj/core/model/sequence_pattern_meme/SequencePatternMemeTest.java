// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern_meme;

import io.xj.core.exception.CoreException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class SequencePatternMemeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SequencePatternMeme()
      .setSequencePatternId(BigInteger.valueOf(23678L))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Pattern ID is required");

    new SequencePatternMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new SequencePatternMeme()
      .setSequencePatternId(BigInteger.valueOf(23678L))
      .validate();
  }

}
