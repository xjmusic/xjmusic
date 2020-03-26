// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.digest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DigestTest {
  @Test
  public void digestType() throws Exception {
    assertEquals(DigestType.DigestMeme, DigestType.validate("DigestMeme"));
    assertEquals(DigestType.DigestChordProgression, DigestType.validate("DigestChordProgression"));
    assertEquals(DigestType.DigestHash, DigestType.validate("DigestHash"));
  }
}
