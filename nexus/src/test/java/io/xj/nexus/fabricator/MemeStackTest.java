// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MemeStackTest {

  @Test
  public void isAllowed() {
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("APPLES")));
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("BANANAS")));
  }

  @Test
  public void isAllowed_preventsAntiMemes() {
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
  public void isAllowed_preventsUniqueMemesFromRepeating() {
    assertTrue(MemeStack.from(List.of("APPLES", "ORANGES")).isAllowed(List.of("$PELICANS")));
    assertTrue(MemeStack.from(List.of("APPLES", "$PELICANS")).isAllowed(List.of("BANANAS")));
    assertFalse(MemeStack.from(List.of("APPLES", "ORANGES", "$PELICANS")).isAllowed(List.of("$PELICANS")));
    assertFalse(MemeStack.from(List.of("APPLES", "$PELICANS")).isAllowed(List.of("BANANAS", "$PELICANS")));
  }
}
