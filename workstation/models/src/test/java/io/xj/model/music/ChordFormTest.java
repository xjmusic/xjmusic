package io.xj.model.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChordFormTest {

  @Test
  public void normalize() {
    assertEquals("", ChordForm.normalize("major"));
    assertEquals("", ChordForm.normalize("maj"));
    assertEquals("", ChordForm.normalize("M"));
    assertEquals("-", ChordForm.normalize("m"));
    assertEquals("-", ChordForm.normalize("minor"));
    assertEquals("-", ChordForm.normalize("min"));
    assertEquals("-", ChordForm.normalize("mi"));
  }
}
