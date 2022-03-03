// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.music;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 Represent a note range
 */
public class NoteRange {
  private static final String UNKNOWN = "Unknown";

  @Nullable
  Note low;

  @Nullable
  Note high;

  private NoteRange() {
    low = null;
    high = null;
  }

  private NoteRange(@Nullable Note low, @Nullable Note high) {
    this.low = low;
    this.high = high;
  }

  private NoteRange(@Nullable String low, @Nullable String high) {
    this.low = Objects.nonNull(low) ? Note.of(low) : null;
    this.high = Objects.nonNull(high) ? Note.of(high) : null;
  }

  public static NoteRange from(@Nullable Note low, @Nullable Note high) {
    return new NoteRange(low, high);
  }

  public static NoteRange from(@Nullable String low, @Nullable String high) {
    return new NoteRange(low, high);
  }

  public static NoteRange copyOf(NoteRange range) {
    return new NoteRange(range.low, range.high);
  }

  public static NoteRange ofNotes(Collection<Note> notes) {
    return new NoteRange(
      notes.stream().min(Note::compareTo).orElse(null),
      notes.stream().max(Note::compareTo).orElse(null)
    );
  }

  public static NoteRange ofStrings(Collection<String> notes) {
    return new NoteRange(
      notes.stream().map(Note::of).min(Note::compareTo).orElse(null),
      notes.stream().map(Note::of).max(Note::compareTo).orElse(null)
    );
  }

  public static NoteRange median(NoteRange r1, NoteRange r2) {
    return new NoteRange(
      Note.median(r1.getLow().orElse(null), r2.getLow().orElse(null)),
      Note.median(r1.getHigh().orElse(null), r2.getHigh().orElse(null))
    );
  }

  public static NoteRange empty() {
    return new NoteRange();
  }

  public Optional<Note> getLow() {
    return Optional.ofNullable(low);
  }

  public Optional<Note> getHigh() {
    return Optional.ofNullable(high);
  }

  public String toString(AdjSymbol adjSymbol) {
    if (Objects.nonNull(low) && Objects.nonNull(high))
      return String.format("%s-%s", low.toString(adjSymbol), high.toString(adjSymbol));
    if (Objects.nonNull(low))
      return String.format("%s", low.toString(adjSymbol));
    if (Objects.nonNull(high))
      return String.format("%s", high.toString(adjSymbol));
    return UNKNOWN;
  }

  public void expand(Collection<Note> notes) {
    for (var note : notes) expand(note);
  }

  public void expand(Note note) {
    if (Objects.isNull(low) || note.isLower(low)) low = note;
    if (Objects.isNull(high) || note.isHigher(high)) high = note;
  }

  public void expand(NoteRange range) {
    if (Objects.nonNull(range.low)) expand(range.low);
    if (Objects.nonNull(range.high)) expand(range.high);
  }

  public int getDeltaSemitones(NoteRange target) {
    var s = getMedianNote();
    var t = target.getMedianNote();
    if (s.isEmpty() || t.isEmpty()) return 0;
    return s.get().delta(t.get());
  }

  public Optional<Note> getMedianNote() {
    if (Objects.isNull(low) && Objects.isNull(high)) return Optional.empty();
    if (Objects.isNull(low)) return Optional.of(high);
    if (Objects.isNull(high)) return Optional.of(low);
    return Optional.of(low.shift(low.delta(high) / 2));
  }

  public NoteRange shifted(int inc) {
    return new NoteRange(
      Objects.nonNull(low) ? low.shift(inc) : null,
      Objects.nonNull(high) ? high.shift(inc) : null
    );
  }

  public boolean isEmpty() {
    return Objects.isNull(low) ||
      Objects.isNull(high) ||
      PitchClass.None.equals(low.getPitchClass()) ||
      PitchClass.None.equals(high.getPitchClass());
  }

  public Optional<Note> getNoteNearestMedian(PitchClass root) {
    var median = getMedianNote();
    if (median.isEmpty()) return Optional.empty();
    if (Objects.equals(root, median.get().getPitchClass())) return median;
    var up = median.get().nextUp(root);
    var down = median.get().nextDown(root);
    return down.delta(median.get()) < median.get().delta(up) ? Optional.of(down) : Optional.of(up);
  }
}
