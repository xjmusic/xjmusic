package io.xj.hub.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    assertEquals("", SlashRoot.of("/G").getPre());
    assertEquals("maj7", SlashRoot.pre("maj7"));
    assertEquals("m", SlashRoot.pre("m/Bb"));
    assertEquals("", SlashRoot.pre("/G"));
  }

  @Test
  public void isPresent() {
    assertFalse(SlashRoot.isPresent("Gm"));
    assertTrue(SlashRoot.isPresent("Gm/Bb"));
  }


  /**
   * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
   * https://www.pivotaltracker.com/story/show/183738228
   */
  @Test
  public void onlyNotes() {
    assertEquals("", SlashRoot.of("C 7/9/13").getPost());
    assertEquals("", SlashRoot.of("C 7/9").getPost());
    assertEquals("E", SlashRoot.of("C 7/E").getPost());
  }

  @Test
  public void isSame() {
    assertTrue(SlashRoot.of("C/E").isSame(SlashRoot.of("A/E")));
  }

  @Test
  public void display() {
    assertEquals("", SlashRoot.of("G 7/9/13").display(Accidental.Sharp));
    assertEquals("/E", SlashRoot.of("A/E").display(Accidental.Sharp));
    assertEquals("/Eb", SlashRoot.of("Ab/Eb").display(Accidental.Flat));
    assertEquals("/D#", SlashRoot.of("Ab/Eb").display(Accidental.Sharp));
  }

  /**
   * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
   * https://www.pivotaltracker.com/story/show/183738228
   */
  @Test
  public void constructor_dontConfuseTensionWithSlash() {
    var tension = SlashRoot.of("C 7/9");
    assertEquals("C 7/9", tension.getPre());
    assertEquals(PitchClass.None, tension.getPitchClass());
    assertEquals("", tension.getPost());
  }


  /**
   * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
   * https://www.pivotaltracker.com/story/show/183738228
   */
  @Test
  public void isPresent_dontConfuseTensionWithSlash() {
    assertTrue(SlashRoot.isPresent("C/E"));
    assertFalse(SlashRoot.isPresent("C/9"));
  }


}
