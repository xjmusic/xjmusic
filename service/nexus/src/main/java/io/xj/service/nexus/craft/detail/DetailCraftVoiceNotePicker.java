package io.xj.service.nexus.craft.detail;

import io.xj.InstrumentAudio;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.music.Note;
import io.xj.lib.music.Tuning;
import io.xj.lib.util.CSV;
import io.xj.service.nexus.fabricator.FabricationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 [#154464276] Detail Craft v1
 */
public class DetailCraftVoiceNotePicker {
  private static final Logger log = LoggerFactory.getLogger(DetailCraftVoiceNotePicker.class);
  private final Key fromKey;
  private final Note fromNote;
  private final SegmentChord segmentChord;
  private final SegmentChordVoicing segmentChordVoicing;
  private final InstrumentAudio instrumentAudio;
  private final Tuning tuning;

  /**
   Construct a new Detail Craft Voice Note Picker

   @param fromKey             to pick from
   @param fromNote            to pick from
   @param segmentChord        to pick from
   @param segmentChordVoicing to pick from
   @param instrumentAudio     to pick from
   @param tuning              to pick from
   */
  public DetailCraftVoiceNotePicker(
    Key fromKey,
    Note fromNote,
    SegmentChord segmentChord,
    SegmentChordVoicing segmentChordVoicing,
    InstrumentAudio instrumentAudio,
    Tuning tuning
  ) {
    this.fromKey = fromKey;
    this.fromNote = fromNote;
    this.segmentChord = segmentChord;
    this.segmentChordVoicing = segmentChordVoicing;
    this.instrumentAudio = instrumentAudio;
    this.tuning = tuning;
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   [#295] Pitch of detail-type instrument audio is altered the least # semitones possible to conform to the current chord

   @param fromNote of voice event
   @param chord    current
   @param voicing  current
   @param audio    that has been picked
   @return final note
   */
  public static DetailCraftVoiceNotePicker from(Key fromKey, Note fromNote, SegmentChord chord, SegmentChordVoicing voicing, InstrumentAudio audio, Tuning tuning) {
    return new DetailCraftVoiceNotePicker(fromKey, fromNote, chord, voicing, audio, tuning);
  }

  /**
   Pick the note

   @return note
   */
  public Note pick() throws FabricationException {
    return CSV.split(segmentChordVoicing.getNotes())
      .stream()
      .map(Note::of)
      .map(note -> new RankedNote(note,
        Math.abs(note.delta(fromNote.transpose(
          fromKey.getRootPitchClass().delta(Chord.of(segmentChord.getName()).getRootPitchClass())
        )))))
      .min(Comparator.comparing(RankedNote::getDelta))
      .orElseThrow(() -> new FabricationException("Failed to pick!"))
      .getNote();
  }

  /**
   Rank a Note based on its delta
   */
  static class RankedNote {
    private final Note note;
    private final int delta;

    public RankedNote(
      Note note,
      int delta
    ) {
      this.note = note;
      this.delta = delta;
    }

    public Note getNote() {
      return note;
    }

    public int getDelta() {
      return delta;
    }
  }
}
