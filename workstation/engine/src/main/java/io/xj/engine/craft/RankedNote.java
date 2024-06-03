// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.craft;

import io.xj.hub.music.Note;

/**
 Rank a Note based on its delta
 <p>
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
