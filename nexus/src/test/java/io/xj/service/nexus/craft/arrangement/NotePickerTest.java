package io.xj.service.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.xj.Instrument;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import org.junit.Test;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 In order to pick exactly one optimal voicing note for each of the source event notes.
 */
public class NotePickerTest {
  NotePicker subject;

  /**
   Based on note-picking test #5 by Mark Stewart
   https://docs.google.com/document/d/1bamMTDDD7XM7_ceh8ecyd9t3h_fy6JSUPVi9RCO2img/
   */
  @Test
  public void arrangementTest5() {
    subject = new NotePicker(
      Instrument.Type.Pad,
      Chord.of("Gsus4/A"),
      new NoteRange(ImmutableList.of(
        "G3",
        "G4"
      )),
      ImmutableSet.of(
        Note.of("F3"),
        Note.of("G3"),
        Note.of("A3"),
        Note.of("C4"),
        Note.of("E4"),
        Note.of("G4"),
        Note.of("A4"),
        Note.of("C5"),
        Note.of("E5"),
        Note.of("G5"),
        Note.of("A5"),
        Note.of("C6")
      ), ImmutableSet.of(
      Note.of("F3"),
      Note.of("A3"),
      Note.of("C4"),
      Note.of("E4")
    )
    );

    subject.pick();

    NoteRange result = subject.getRange();
    assertEquals("G3", result.getLow().orElseThrow().toString(AdjSymbol.Sharp));
    assertEquals("G4", result.getHigh().orElseThrow().toString(AdjSymbol.Sharp));
    assertEquals(ImmutableSet.of("G3", "A3", "C4", "E4"),
      subject.getPickedNotes().stream().map(n -> n.toString(AdjSymbol.Sharp)).collect(Collectors.toSet()));
  }
}
