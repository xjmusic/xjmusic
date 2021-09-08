package io.xj.lib.music;

import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.lib.util.CSV;

public enum Voicing {
  ;

  public static boolean containsAnyValidNotes(String notes) {
    return CSV.split(notes).stream().anyMatch(Note::isValid);
  }

  public static boolean containsAnyValidNotes(ProgramSequenceChordVoicing programSequenceChordVoicing) {
    return containsAnyValidNotes(programSequenceChordVoicing.getNotes());
  }
}
