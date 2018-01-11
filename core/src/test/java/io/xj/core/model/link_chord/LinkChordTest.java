// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_chord;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class LinkChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new LinkChord()
      .setName("C# minor")
      .setLinkId(BigInteger.valueOf(1235))
      .setPosition(7)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new LinkChord()
      .setLinkId(BigInteger.valueOf(1235))
      .setPosition(7)
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new LinkChord()
      .setName("C# minor")
      .setPosition(7)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new LinkChord()
      .setName("C# minor")
      .setLinkId(BigInteger.valueOf(1235))
      .validate();
  }

}
