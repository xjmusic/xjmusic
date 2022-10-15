package io.xj.lib.music;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChordDescriptionTest {

  @Test
  public void normalize() {
    assertEquals("", ChordDescription.normalize("major"));
    assertEquals("", ChordDescription.normalize("maj"));
    assertEquals("", ChordDescription.normalize("M"));
    assertEquals("-", ChordDescription.normalize("m"));
    assertEquals("-", ChordDescription.normalize("minor"));
    assertEquals("-", ChordDescription.normalize("min"));
    assertEquals("-", ChordDescription.normalize("mi"));
  }
}
