package io.xj.service.nexus.craft.arrangement;

import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.lib.music.Key;
import io.xj.lib.music.MusicalException;
import io.xj.lib.music.Note;
import io.xj.lib.music.PitchClass;
import io.xj.lib.music.Tuning;
import io.xj.service.nexus.craft.arrangement.ArrangementVoiceNotePicker;
import io.xj.service.nexus.fabricator.FabricationException;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ArrangementVoiceNotePickerTest {

  @Test
  public void pick() throws MusicalException, FabricationException {
    Tuning tuning = Tuning.atA4(432.0);
    String segmentId = UUID.randomUUID().toString();
    String instrumentId = UUID.randomUUID().toString();
    InstrumentAudio audio = InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrumentId)
      .setName("Test audio")
      .setWaveformKey("fake.audio5.wav")
      .setStart(0)
      .setLength(2)
      .setTempo(120)
      .setPitch(300)
      .setDensity(0.5)
      .build();
    SegmentChord chord = SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segmentId)
      .setPosition(8.0)
      .setName("A minor 7")
      .build();
    SegmentChordVoicing voicing = SegmentChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segmentId)
      .setSegmentChordId(chord.getId())
      .setType(Instrument.Type.Pad)
      .setNotes("A4, C5, E5, G5, A5, C6, G6, E6")
      .build();
    Note note = Note.of("Bb4");
    Key key = Key.of("G minor");

    ArrangementVoiceNotePicker subject = new ArrangementVoiceNotePicker(key, note, chord, voicing, audio, tuning);

    Note result = subject.pick();
    assertEquals(PitchClass.C, result.getPitchClass());
    assertEquals(5, result.getOctave().intValue());
  }
}
