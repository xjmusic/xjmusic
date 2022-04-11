package io.xj.lib.meme;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 TemplateConfig has Meme categories
 https://www.pivotaltracker.com/story/show/181801646
 <p>
 A template configuration has a field called `memeTaxonomy` which defines the taxonomy of memes.
 <p>
 For example, this might look like
 <p>
 ```
 memeTaxonomy=CITY[CHICAGO,DENVER,PHILADELPHIA]
 ```
 <p>
 That would tell XJ about the existence of a meme category called City with values `CHICAGO`, `DENVER`, and `PHILADELPHIA`. And these would function as exclusion like numeric memes, e.g. after content having `CHICAGO` is chosen, we can choose nothing with `DENVER` or `PHILADELPHIA`.
 */
public class MemeTaxonomyTest {
  @Test
  public void testToString() {
    var subject = MemeTaxonomy.fromString("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]");

    assertEquals("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]", subject.toString());
  }

  @Test
  public void testStripNonAlphabetical() {
    var subject = MemeTaxonomy.fromString("COLOR [RED, GREEN, BLUE];    SIZE [LARGE, MEDIUM, SMALL ]");

    assertEquals("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]", subject.toString());
  }

  @Test
  public void getCategories() {
    var subject = MemeTaxonomy.fromString("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]");

    assertEquals(2, subject.getCategories().size());
    assertEquals("COLOR", subject.getCategories().get(0).getName());
    assertEquals("RED", subject.getCategories().get(0).getMemes().get(0));
    assertEquals("GREEN", subject.getCategories().get(0).getMemes().get(1));
    assertEquals("BLUE", subject.getCategories().get(0).getMemes().get(2));
    assertEquals("SIZE", subject.getCategories().get(1).getName());
    assertEquals("LARGE", subject.getCategories().get(1).getMemes().get(0));
    assertEquals("MEDIUM", subject.getCategories().get(1).getMemes().get(1));
    assertEquals("SMALL", subject.getCategories().get(1).getMemes().get(2));
  }

  @Test
  public void isAllowed() {
    assertTrue(MemeTaxonomy.fromString("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed(List.of("PEACHES")));
    assertTrue(MemeTaxonomy.fromString("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed(List.of("DENVER")));
    assertFalse(MemeTaxonomy.fromString("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed(List.of("DENVER", "PHILADELPHIA")));
    assertTrue(MemeTaxonomy.fromString("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed(List.of("RED", "LARGE")));
    assertTrue(MemeTaxonomy.fromString("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed(List.of("GREEN", "MEDIUM", "PEACHES")));
    assertFalse(MemeTaxonomy.fromString("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed(List.of("RED", "BLUE", "LARGE")));
    assertFalse(MemeTaxonomy.fromString("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed(List.of("GREEN", "MEDIUM", "PEACHES", "SMALL")));
  }

}
