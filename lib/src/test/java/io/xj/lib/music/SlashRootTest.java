package io.xj.lib.music;

import org.junit.Before;
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
}
