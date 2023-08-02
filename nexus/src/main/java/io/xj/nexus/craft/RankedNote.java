// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.lib.music.Note;

/**
 * Rank a Note based on its delta
 * <p>
 */
public class RankedNote {
  final Note note;
  final int delta;

  public RankedNote(
    Note note,
    int delta
  ) {
    this.note = note;
    this.delta = delta;
  }

  public Note getTones() {
    return note;
  }

  public int getDelta() {
    return delta;
  }
}
