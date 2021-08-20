package io.xj.lib.music;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 Represent a note range
 */
public class NoteRange {
  private static final String UNKNOWN = "Unknown";

  @Nullable
  Note low;

  @Nullable
  Note high;

  public NoteRange(Collection<String> notes) {
    low = notes.stream().map(Note::of).min(Note::compareTo).orElse(null);
    high = notes.stream().map(Note::of).max(Note::compareTo).orElse(null);
  }

  public NoteRange() {
    low = null;
    high = null;
  }

  public NoteRange(@Nullable Note low, @Nullable Note high) {
    this.low = low;
    this.high = high;
  }

  public NoteRange(@Nullable String low, @Nullable String high) {
    this.low = Objects.nonNull(low) ? Note.of(low) : null;
    this.high = Objects.nonNull(high) ? Note.of(high) : null;
  }

  public static NoteRange copyOf(NoteRange range) {
    return new NoteRange(range.low, range.high);
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

  public void expand(Set<Note> notes) {
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
}
