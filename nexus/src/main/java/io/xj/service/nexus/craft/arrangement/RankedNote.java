package io.xj.service.nexus.craft.arrangement;

import io.xj.lib.music.Note;

/**
 Rank a Note based on its delta
 <p>
 */
public class RankedNote {
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
