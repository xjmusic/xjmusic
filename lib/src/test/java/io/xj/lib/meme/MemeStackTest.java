// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.meme;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MemeStackTest {

  /**
   Basics: all memes are allowed
   */
  @Test
  public void isAllowed() {
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("APPLES")));
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("BANANAS")));
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of()));
    assertTrue(MemeStack.from(List.of()).isAllowed(List.of("BANANAS")));
  }

  /**
   Anti-Memes
   <p>
   Artist can add !MEME values into Programs #176474073
   */
  @Test
  public void antiMemes() {
    assertTrue(MemeStack.from(List.of("!APPLES", "ORANGES")).isAllowed(List.of("!APPLES")));
    assertTrue(MemeStack.from(List.of("!APPLES", "!ORANGES")).isAllowed(List.of("!APPLES", "!ORANGES", "BANANAS")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("!APPLES")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("!ORANGES")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("!ORANGES", "APPLES")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("ORANGES", "!APPLES")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("!ORANGES", "!APPLES")));
    assertFalse(MemeStack.from(List.of("!APPLES", "ORANGES")).isAllowed(List.of("!APPLES", "!ORANGES")));
  }

  /**
   Unique Memes
   <p>
   Artist can add `$MEME` so only one is chosen #179078760
   */
  @Test
  public void uniqueMemes() {
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("$PELICANS")));
    assertTrue(MemeStack.from(List.of("APPLES", "$PELICANS")).isAllowed(List.of("BANANAS")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES", "$PELICANS")).isAllowed(List.of("$PELICANS")));
    assertFalse(MemeStack.from(List.of("APPLES", "$PELICANS")).isAllowed(List.of("BANANAS", "$PELICANS")));
  }

  /**
   Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive #180125852
   */
  @Test
  public void numericMemes() {
    assertEquals(5, (int) MmNumeric.fromString("5BEAT").prefix);
    assertEquals("STEP", MmNumeric.fromString("2STEP").body);
    assertTrue("STEP", MmNumeric.fromString("2STEP").isValid);
    assertNull(MmNumeric.fromString("JAMMY").prefix);
    assertNull(MmNumeric.fromString("JAMMY").body);
    assertFalse(MmNumeric.fromString("JAMMY").isValid);
    assertTrue(MemeStack.from(List.of("JAMS", "2STEP")).isAllowed(List.of("2STEP", "4NOTE")));
    assertTrue(MemeStack.from(List.of("JAMS", "4NOTE", "2STEP")).isAllowed(List.of("2STEP", "4NOTE")));
    assertFalse(MemeStack.from(List.of("JAMS", "2STEP")).isAllowed(List.of("4STEP", "4NOTE")));
    assertFalse(MemeStack.from(List.of("JAMS", "2STEP", "4NOTE")).isAllowed(List.of("2STEP", "3NOTE")));
  }

  /**
   Strong-meme like LEMONS! should always favor LEMONS #180468772
   */
  @Test
  public void strongMemes() {
    assertEquals("LEMONS", MmStrong.fromString("LEMONS!").body);
    assertTrue(MmStrong.fromString("LEMONS!").isValid);
    assertFalse(MmStrong.fromString("LEMONS").isValid);
    assertTrue(MemeStack.from(List.of("JAMS", "LEMONS!")).isAllowed(List.of("4NOTE", "LEMONS")));
    assertFalse(MemeStack.from(List.of("JAMS", "LEMONS!")).isAllowed(List.of("4NOTE", "ORANGES")));
  }

}
