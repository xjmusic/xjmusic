// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.music;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

/**
 * Represent a note range
 */
public class NoteRange {
  static final String UNKNOWN = "Unknown";
  static final int MAX_SEEK_OCTAVES = 3;

  @Nullable
  Note low;

  @Nullable
  Note high;

  @Nullable
  Integer span;

  NoteRange() {
    low = null;
    high = null;
    span = 0;
  }

  NoteRange(@Nullable Note low, @Nullable Note high) {
    this.low = low;
    this.high = high;
    span = Objects.nonNull(low) && Objects.nonNull(high) ? low.delta(high) : null;
  }

  NoteRange(@Nullable String low, @Nullable String high) {
    this.low = Objects.nonNull(low) ? Note.of(low) : null;
    this.high = Objects.nonNull(high) ? Note.of(high) : null;
    span = Objects.nonNull(low) && Objects.nonNull(high) ? this.low.delta(this.high) : null;
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
    return new NoteRange(notes.parallelStream().min(Note::compareTo).orElse(null), notes.parallelStream().max(Note::compareTo).orElse(null));
  }

  public static NoteRange ofStrings(Collection<String> notes) {
    return new NoteRange(notes.parallelStream().map(Note::of).min(Note::compareTo).orElse(null), notes.parallelStream().map(Note::of).max(Note::compareTo).orElse(null));
  }

  public static NoteRange median(NoteRange r1, NoteRange r2) {
    return new NoteRange(Note.median(r1.getLow().orElse(null), r2.getLow().orElse(null)), Note.median(r1.getHigh().orElse(null), r2.getHigh().orElse(null)));
  }

  public static NoteRange empty() {
    return new NoteRange();
  }

  /**
   * Compute the median optimal range shift octaves
   *
   * @param sourceRange from
   * @param targetRange to
   * @return median optimal range shift octaves
   */
  public static Integer computeMedianOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws MusicalException {
    if (sourceRange.getLow().isEmpty() || sourceRange.getHigh().isEmpty() || targetRange.getLow().isEmpty() || targetRange.getHigh().isEmpty())
      return 0;
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is the lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int dLow = targetRange.getLow().orElseThrow(() -> new MusicalException("Can't find low end of target range")).delta(sourceRange.getLow().orElseThrow(() -> new MusicalException("Can't find low end of source range")).shiftOctave(o));
      int dHigh = targetRange.getHigh().orElseThrow(() -> new MusicalException("Can't find high end of target range")).delta(sourceRange.getHigh().orElseThrow(() -> new MusicalException("Can't find high end of source range")).shiftOctave(o));
      if (0 <= dLow && 0 >= dHigh && Math.abs(o) < baselineDelta) {
        baselineDelta = Math.abs(o);
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  public Optional<Note> getLow() {
    return Optional.ofNullable(low);
  }

  public Optional<Note> getHigh() {
    return Optional.ofNullable(high);
  }

  public Optional<Integer> getSpan() {
    return Optional.ofNullable(span);
  }

  public String toString(Accidental accidental) {
    if (Objects.nonNull(low) && Objects.nonNull(high))
      return String.format("%s-%s", low.toString(accidental), high.toString(accidental));
    if (Objects.nonNull(low)) return String.format("%s", low.toString(accidental));
    if (Objects.nonNull(high)) return String.format("%s", high.toString(accidental));
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
    return new NoteRange(Objects.nonNull(low) ? low.shift(inc) : null, Objects.nonNull(high) ? high.shift(inc) : null);
  }

  public boolean isEmpty() {
    return Objects.isNull(low) || Objects.isNull(high) || PitchClass.None.equals(low.getPitchClass()) || PitchClass.None.equals(high.getPitchClass());
  }

  public Optional<Note> getNoteNearestMedian(PitchClass root) {
    if (PitchClass.None.equals(root)) return Optional.empty();
    var median = getMedianNote();
    if (median.isEmpty()) return Optional.empty();
    if (Objects.equals(root, median.get().getPitchClass())) return median;
    var up = median.get().nextUp(root);
    var down = median.get().nextDown(root);
    return down.delta(median.get()) < median.get().delta(up) ? Optional.of(down) : Optional.of(up);
  }

  /**
   * Change the octave of a note such that it is within this range
   *
   * @param note source
   * @return note moved to available octave
   */
  public Note toAvailableOctave(Note note) {
    if (Objects.isNull(low) || Objects.isNull(high)) return note;

    int d = 0;
    Note x = note;

    while (!includes(x) && d < MAX_SEEK_OCTAVES) {
      if (low.isHigher(x)) {
        x = x.shiftOctave(1);
        d++;
      } else if (high.isLower(x)) {
        x = x.shiftOctave(-1);
        d++;
      }
    }

    return x;
  }

  /**
   * Whether the given note is within this range
   *
   * @param note to test
   * @return true if note is within this range
   */
  public boolean includes(Note note) {
    if (Objects.isNull(low) && Objects.isNull(high)) return false;
    if (Objects.isNull(low) && high.sameAs(note)) return true;
    if (Objects.isNull(high) && low.sameAs(note)) return true;
    if (Objects.isNull(low) || Objects.isNull(high)) return false;
    return !low.isHigher(note) && !high.isLower(note);
  }
}
