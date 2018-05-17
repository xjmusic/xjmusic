// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_meme;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class SequenceMemeTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SequenceMeme()
      .setSequenceId(BigInteger.valueOf(23678L))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Sequence ID is required");

    new SequenceMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new SequenceMeme()
      .setSequenceId(BigInteger.valueOf(23678L))
      .validate();
  }

}
