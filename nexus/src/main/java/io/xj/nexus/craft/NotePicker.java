// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

/**
 In order to pick exactly one optimal voicing note for each of the source event notes.
 */
public class NotePicker {
  private final NoteRange targetRange;
  private final Set<Note> eventNotes;
  private final Set<Note> voicingNotes;
  private final Set<Note> pickedNotes;
  private final boolean seekInversions;
  private final SecureRandom random = new SecureRandom(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
  private final NoteRange voicingRange;

  /**
   Build a NotePicker from the given optimal target range

   @param targetRange          optimally picks will be within
   @param voicingNotes   to pick from, at most once each
   @param eventNotes     for which to pick exactly one voicing note each
   @param seekInversions whether to seek inversions
   */
  public NotePicker(NoteRange targetRange, Collection<Note> voicingNotes, Collection<Note> eventNotes, boolean seekInversions) {
    this.targetRange = NoteRange.copyOf(targetRange);
    this.eventNotes = new HashSet<>(eventNotes);
    this.voicingNotes = new HashSet<>(voicingNotes);
    this.voicingRange = NoteRange.ofNotes(voicingNotes);
    this.seekInversions = seekInversions;
    pickedNotes = new HashSet<>();
  }

  /**
   Pick all voicing notes for event notes
   */
  public void pick() {

    // Pick the notes
    for (var eN : eventNotes.stream()
      .map(voicingRange::toAvailableOctave)
      .sorted(Note::compareTo)
      .toList()) {
      if (PitchClass.None.equals(eN.getPitchClass()))
        pickRandom(voicingNotes).ifPresent(this::pick);
      else
        voicingNotes.stream()
          .sorted(Note::compareTo)
          .map(vN -> new RankedNote(vN, Math.abs(vN.delta(eN))))
          .min(Comparator.comparing(RankedNote::getDelta))
          .map(RankedNote::getNote)
          .map(voicingNote -> seekInversion(voicingNote, targetRange, voicingNotes))
          .ifPresent(this::pick);
    }

    // If nothing has made it through to here, pick a single atonal note.
    if (pickedNotes.isEmpty()) pickedNotes.add(Note.of(Note.ATONAL));

    // Keep track of the total range of notes selected, to keep voicing in the tightest possible range
    targetRange.expand(pickedNotes);
  }

  /**
   @return range of picked notes (updated after picking)
   */
  public NoteRange getTargetRange() {
    return targetRange;
  }

  /**
   @return resulting notes picked
   */
  public Set<Note> getPickedNotes() {
    return pickedNotes;
  }

  /**
   Pick a note, adding it to picked notes and removing it from voicing notes

   @param voicingNote to pick
   */
  private void pick(Note voicingNote) {
    voicingNotes.remove(voicingNote);
    pickedNotes.add(voicingNote);
  }

  /**
   Seek the inversion of the given note that is best contained within the given range

   @param source  for which to seek inversion
   @param range   towards which seeking will optimize
   @param options from which to select better notes
   */
  private Note seekInversion(Note source, NoteRange range, Collection<Note> options) {
    if (!seekInversions) return source;

    if (range.getHigh().isPresent() && range.getHigh()
      .orElseThrow(() -> new RuntimeException("Can't get high end of range"))
      .isLower(source)) {
      var alt = options
        .stream()
        .filter(o -> !range.getHigh().get().isLower(o))
        .map(o -> new RankedNote(o,
          Math.abs(o.delta(range.getHigh().get()))))
        .min(Comparator.comparing(RankedNote::getDelta))
        .map(RankedNote::getNote);
      if (alt.isPresent()) return alt.get();
    }

    if (range.getLow().isPresent() && range.getLow()
      .orElseThrow(() -> new RuntimeException("Can't get low end of range"))
      .isHigher(source)) {
      var alt = options
        .stream()
        .filter(o -> !range.getLow().get().isHigher(o))
        .map(o -> new RankedNote(o,
          Math.abs(o.delta(range.getLow().get()))))
        .min(Comparator.comparing(RankedNote::getDelta))
        .map(RankedNote::getNote);
      if (alt.isPresent()) return alt.get();
    }

    return source;
  }

  /**
   Pick a random instrument note from the available notes in the voicing
   <p>
   [#175947230] Artist writing detail program expects 'X' note value to result in random selection from available Voicings

   @param voicingNotes to pick from
   @return a random note from the voicing
   */
  private Optional<Note> pickRandom(Collection<Note> voicingNotes) {
    return voicingNotes
      .stream()
      .sorted(Comparator.comparing((s) -> random.nextFloat()))
      .findAny();
  }
}
