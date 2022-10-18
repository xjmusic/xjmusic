package io.xj.lib.music;

import org.junit.Test;

import static org.junit.Assert.*;

public class SlashRootTest {

  @Test
  public void of() {
    assertEquals(PitchClass.As, SlashRoot.of("Gm/Bb").getPitchClass());
  }

  @Test
  public void getPitchClass() {
    assertEquals(PitchClass.G, SlashRoot.of("Eb/G").getPitchClass());
  }

  @Test
  public void orDefault() {
    assertEquals(PitchClass.As, SlashRoot.of("Eb").orDefault(PitchClass.As));
  }

  @Test
  public void pre() {
    assertEquals("Gm", SlashRoot.pre("Gm"));
    assertEquals("Gm", SlashRoot.pre("Gm/Bb"));
  }

  @Test
  public void isPresent() {
    assertFalse(SlashRoot.isPresent("Gm"));
    assertTrue(SlashRoot.isPresent("Gm/Bb"));
  }

  @Test
  public void alphanumeric() {
    assertEquals("9/13", SlashRoot.of("C 7/9/13").getPost());
    assertEquals("9", SlashRoot.of("C 7/9").getPost());
    assertEquals("E", SlashRoot.of("C 7/E").getPost());
  }

  @Test
  public void isSame() {
    assertTrue(SlashRoot.of("C 7/9/13").isSame(SlashRoot.of("G 7/9/13")));
    assertTrue(SlashRoot.of("C/E").isSame(SlashRoot.of("A/E")));
  }

  @Test
  public void display() {
    assertEquals("/9/13", SlashRoot.of("G 7/9/13").display(AdjSymbol.Sharp));
    assertEquals("/E", SlashRoot.of("A/E").display(AdjSymbol.Sharp));
    assertEquals("/Eb", SlashRoot.of("Ab/Eb").display(AdjSymbol.Flat));
    assertEquals("/D#", SlashRoot.of("Ab/Eb").display(AdjSymbol.Sharp));
  }
}
