//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft.digest;

import org.junit.Test;

import static org.junit.Assert.*;

public class DigestTest {
  @Test
  public void digestType() throws Exception {
    assertEquals(DigestType.DigestMeme, DigestType.validate("DigestMeme"));
    assertEquals(DigestType.DigestChordProgression, DigestType.validate("DigestChordProgression"));
    assertEquals(DigestType.DigestHash, DigestType.validate("DigestHash"));
  }
}
