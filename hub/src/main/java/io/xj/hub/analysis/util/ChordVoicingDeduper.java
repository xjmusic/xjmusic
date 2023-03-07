package io.xj.hub.analysis.util;

import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramVoice;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Chord Search while composing a main program
 * https://www.pivotaltracker.com/story/show/178921705
 * <p>
 * Chord search results (backend) must include all unique combinations of chord & voicing
 */
public class ChordVoicingDeduper {
  private final Map<UUID, ProgramVoice> voicesById;
  private final Collection<ProgramSequenceChord> chords;
  private final Collection<ProgramSequenceChordVoicing> voicings;
  private static final String FINGERPRINT_SEPARATOR_MINOR = "|---|";
  private static final String FINGERPRINT_SEPARATOR_MAJOR = "|-----|";

  public ChordVoicingDeduper(
    Collection<ProgramVoice> voices,
    Collection<ProgramSequenceChord> chords,
    Collection<ProgramSequenceChordVoicing> voicings
  ) {
    this.voicesById = voices.parallelStream()
      .collect(Collectors.toMap(ProgramVoice::getId, (v) -> v));

    var uniqueVoicings = chords.parallelStream()
      .map(chord -> new ChordVoicings(chord,
        voicings.parallelStream()
          .filter(v -> chord.getId().equals(v.getProgramSequenceChordId()))
          .toList()))
      .collect(Collectors.toMap(
        (cv) -> String.format("%s%s%s", cv.getChord().getName().toLowerCase(Locale.ROOT),
          FINGERPRINT_SEPARATOR_MAJOR,
          cv.getVoicings().parallelStream()
            .sorted(Comparator.comparing((v) -> voicesById.get(v.getProgramVoiceId()).getType()))
            .map(ProgramSequenceChordVoicing::getNotes)
            .map(String::toLowerCase)
            .collect(Collectors.joining(FINGERPRINT_SEPARATOR_MINOR))),
        (cv) -> cv,
        (cv1, cv2) -> cv2
      ));

    this.chords = uniqueVoicings.values().parallelStream()
      .map(ChordVoicings::getChord)
      .toList();

    this.voicings = uniqueVoicings.values().parallelStream()
      .flatMap(cv -> cv.getVoicings().parallelStream())
      .toList();
  }

  public Collection<ProgramSequenceChord> getChords() {
    return chords;
  }

  public Collection<ProgramSequenceChordVoicing> getVoicings() {
    return voicings;
  }


  /**
   * One chord and its voicings
   */
  public record ChordVoicings(
    ProgramSequenceChord chord,
    Collection<ProgramSequenceChordVoicing> voicings
  ) {
    public ProgramSequenceChord getChord() {
      return chord;
    }

    public Collection<ProgramSequenceChordVoicing> getVoicings() {
      return voicings;
    }
  }

}
