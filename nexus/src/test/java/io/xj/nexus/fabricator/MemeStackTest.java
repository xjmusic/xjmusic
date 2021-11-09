// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class MemeStackTest {

  @Test
  public void isAllowed() {
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("APPLES")));
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("BANANAS")));
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of()));
    assertTrue(MemeStack.from(List.of()).isAllowed(List.of("BANANAS")));
  }

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
    assertEquals(5, (int) new MemeStack.NumericMeme("5BEAT").prefix);
    assertEquals("STEP", new MemeStack.NumericMeme("2STEP").body);
    assertTrue("STEP", new MemeStack.NumericMeme("2STEP").isValid);
    assertNull(new MemeStack.NumericMeme("JAMMY").prefix);
    assertNull(new MemeStack.NumericMeme("JAMMY").body);
    assertFalse(new MemeStack.NumericMeme("JAMMY").isValid);
    assertTrue(MemeStack.from(List.of("JAMS", "2STEP")).isAllowed(List.of("2STEP", "4NOTE")));
    assertTrue(MemeStack.from(List.of("JAMS", "4NOTE", "2STEP")).isAllowed(List.of("2STEP", "4NOTE")));
    assertFalse(MemeStack.from(List.of("JAMS", "2STEP")).isAllowed(List.of("4STEP", "4NOTE")));
    assertFalse(MemeStack.from(List.of("JAMS", "2STEP", "4NOTE")).isAllowed(List.of("2STEP", "3NOTE")));
  }
}
