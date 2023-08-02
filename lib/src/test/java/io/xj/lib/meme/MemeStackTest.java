// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.meme;

import java.util.Map;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MemeStackTest {

  /**
   * Basics: all memes are allowed
   */
  @Test
  public void isAllowed() {
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("APPLES")));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("BANANAS")));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of()));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of()).isAllowed(List.of("BANANAS")));
  }

  /**
   * Anti-Memes
   * <p>
   * Artist can add !MEME values into Programs https://www.pivotaltracker.com/story/show/176474073
   */
  @Test
  public void antiMemes() {
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("!APPLES", "ORANGES")).isAllowed(List.of("!APPLES")));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("!APPLES", "!ORANGES")).isAllowed(List.of("!APPLES", "!ORANGES", "BANANAS")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("!APPLES")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("!ORANGES")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("!ORANGES", "APPLES")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("ORANGES", "!APPLES")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("!ORANGES", "!APPLES")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("!APPLES", "ORANGES")).isAllowed(List.of("!APPLES", "!ORANGES")));
  }

  /**
   * Unique Memes
   * <p>
   * Artist can add `$MEME` so only one is chosen https://www.pivotaltracker.com/story/show/179078760
   */
  @Test
  public void uniqueMemes() {
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES")).isAllowed(List.of("$PELICANS")));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "$PELICANS")).isAllowed(List.of("BANANAS")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "ORANGES", "$PELICANS")).isAllowed(List.of("$PELICANS")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "$PELICANS")).isAllowed(List.of("BANANAS", "$PELICANS")));
  }

  /**
   * Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive https://www.pivotaltracker.com/story/show/180125852
   */
  @Test
  public void numericMemes() {
    assertEquals(5, (int) ParseNumeric.fromString("5BEAT").prefix);
    assertEquals("STEP", ParseNumeric.fromString("2STEP").body);
    assertTrue("STEP", ParseNumeric.fromString("2STEP").isValid);
    assertNull(ParseNumeric.fromString("JAMMY").prefix);
    assertNull(ParseNumeric.fromString("JAMMY").body);
    assertFalse(ParseNumeric.fromString("JAMMY").isValid);
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "2STEP")).isAllowed(List.of("2STEP", "4NOTE")));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "4NOTE", "2STEP")).isAllowed(List.of("2STEP", "4NOTE")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "2STEP")).isAllowed(List.of("4STEP", "4NOTE")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "2STEP", "4NOTE")).isAllowed(List.of("2STEP", "3NOTE")));
  }

  /**
   * Strong-meme like LEMONS! should always favor LEMONS https://www.pivotaltracker.com/story/show/180468772
   */
  @Test
  public void strongMemes() {
    assertEquals("LEMONS", ParseStrong.fromString("LEMONS!").body);
    assertTrue(ParseStrong.fromString("LEMONS!").isValid);
    assertFalse(ParseStrong.fromString("LEMONS").isValid);
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "LEMONS!")).isAllowed(List.of("4NOTE", "LEMONS")));
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "ORANGES")).isAllowed(List.of("4NOTE", "LEMONS!")));
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("JAMS", "LEMONS!")).isAllowed(List.of("4NOTE", "ORANGES")));
  }

  /**
   * Strong-meme like LEMONS! should always favor LEMONS https://www.pivotaltracker.com/story/show/180468772
   */
  @Test
  public void strongMemes_okayToAddBothStrongAndRegular_butNotOnlyStrong() {
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("LEMONS", "LEMONS!")).isValid());
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("LEMONS!")).isValid());
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("LEMONS")).isAllowed(List.of("LEMONS!")));
  }

  @Test
  public void strongMemes_withTaxonomy() {
    var taxonomy = MemeTaxonomy.fromList(List.of(
      Map.of(
        "name", "VOXHOOK",
        "memes", List.of("DONTLOOK", "NEEDU", "FLOAT", "ALLGO")
      ),
      Map.of(
        "name", "SEASON",
        "memes", List.of("WINTER", "SPRING", "SUMMER", "FALL")
      )));

    assertTrue(MemeStack.from(taxonomy, List.of(
      "!TIGHTY",
      "4NOTE",
      "ALLGO",
      "EARTH",
      "FIRE",
      "KNOCKY",
      "LARGE",
      "NEW",
      "OPEN",
      "SMALL",
      "STRAIGHT",
      "STRONGMEME",
      "WATER",
      "WIDEOPEN",
      "WIND")
    ).isAllowed(List.of(
      "SMALL",
      "FIRE",
      "STRONGMEME!"
    )));
  }

  /**
   * TemplateConfig has Meme categories
   * https://www.pivotaltracker.com/story/show/181801646
   * <p>
   * A template configuration has a field called `memeTaxonomy` which defines the taxonomy of memes.
   * <p>
   * For example, this might look like
   * <p>
   * ```
   * memeTaxonomy=CITY[CHICAGO,DENVER,PHILADELPHIA]
   * ```
   * <p>
   * That would tell XJ about the existence of a meme category called City with values `CHICAGO`, `DENVER`, and `PHILADELPHIA`. And these would function as exclusion like numeric memes, e.g. after content having `CHICAGO` is chosen, we can choose nothing with `DENVER` or `PHILADELPHIA`.
   */
  @Test
  public void memeCategories() {
    var taxonomy = MemeTaxonomy.fromString("CITY[CHICAGO,DENVER,PHILADELPHIA]");

    assertTrue(MemeStack.from(taxonomy, List.of("CHICAGO", "ORANGES")).isAllowed(List.of("PEACHES")));
    assertFalse(MemeStack.from(taxonomy, List.of("CHICAGO", "ORANGES")).isAllowed(List.of("DENVER")));
    assertTrue(MemeStack.from(taxonomy, List.of("CHICAGO", "ORANGES")).isValid());
    assertTrue(MemeStack.from(taxonomy, List.of("DENVER", "ORANGES")).isValid());
    assertFalse(MemeStack.from(taxonomy, List.of("CHICAGO", "DENVER", "ORANGES")).isValid());
  }

  @Test
  public void memeCategories_allowAlreadyPresentFromTaxonomy() {
    var taxonomy = MemeTaxonomy.fromString("CITY[ABERDEEN,NAGOYA]");

    assertTrue(MemeStack.from(taxonomy, List.of("ABERDEEN")).isAllowed(List.of("ABERDEEN")));
  }

  /**
   * Refuse to make a choice that violates the meme stack https://www.pivotaltracker.com/story/show/181466514
   */
  @Test
  public void isValid() {
    assertTrue(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "!ORANGES", "$BANANAS", "APPLES!", "5LEMONS", "12MONKEYS")).isValid());
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "!APPLES", "$BANANAS", "APPLES!", "5LEMONS", "12MONKEYS")).isValid());
    assertFalse(MemeStack.from(MemeTaxonomy.empty(), List.of("APPLES", "!ORANGES", "$BANANAS", "APPLES!", "5LEMONS", "12LEMONS")).isValid());
  }

}
