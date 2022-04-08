package io.xj.hub.analysis.util;

import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 Chord Search while composing a main program
 https://www.pivotaltracker.com/story/show/178921705
 <p>
 Chord search results (backend) must include all unique combinations of chord & voicing
 */
public record ChordVoicingDeduper(
  Collection<ProgramSequenceChord> chords,
  Collection<ProgramSequenceChordVoicing> voicings
) {

  public ChordVoicingDeduper(
    Collection<ProgramSequenceChord> chords,
    Collection<ProgramSequenceChordVoicing> voicings
  ) {
    var chordVoicings = chords.stream()
      .map(chord -> new ChordVoicings(chord,
        voicings.stream()
          .filter(v -> chord.getId().equals(v.getProgramSequenceChordId()))
          .toList()))
      .collect(Collectors.toMap(
        ChordVoicings::getFingerprint,
        (cv) -> cv,
        (cv1, cv2) -> cv2
      ));

    this.chords = chordVoicings.values().stream()
      .map(ChordVoicings::getChord)
      .toList();
    this.voicings = chordVoicings.values().stream()
      .flatMap(cv -> cv.getVoicings().stream())
      .toList();
  }

  public Collection<ProgramSequenceChord> getChords() {
    return chords;
  }

  public Collection<ProgramSequenceChordVoicing> getVoicings() {
    return voicings;
  }

  /**
   One chord and its voicings
   */
  public record ChordVoicings(
    ProgramSequenceChord chord,
    Collection<ProgramSequenceChordVoicing> voicings
  ) {

    private static final String FINGERPRINT_SEPARATOR_MINOR = "|---|";
    private static final String FINGERPRINT_SEPARATOR_MAJOR = "|-----|";

    public ProgramSequenceChord getChord() {
      return chord;
    }

    public Collection<ProgramSequenceChordVoicing> getVoicings() {
      return voicings;
    }

    /**
     @return the unique fingerprint of this chord and its voicings
     */
    public String getFingerprint() {
      return String.format("%s%s%s",
        chord.getName().toLowerCase(Locale.ROOT),
        FINGERPRINT_SEPARATOR_MAJOR,
        voicings.stream()
          .sorted(Comparator.comparing(ProgramSequenceChordVoicing::getType))
          .map(ProgramSequenceChordVoicing::getNotes)
          .map(String::toLowerCase)
          .collect(Collectors.joining(FINGERPRINT_SEPARATOR_MINOR)));
    }
  }

}
