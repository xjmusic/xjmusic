// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;
import io.xj.lib.util.MarbleBag;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * In order to pick exactly one optimal voicing note for each of the source event notes.
 */
public class NotePicker {
  final NoteRange targetRange;
  final Set<Note> voicingNotes;
  final boolean seekInversions;
  final SecureRandom random = new SecureRandom(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
  final NoteRange voicingRange;

  /**
   * Build a NotePicker from the given optimal target range
   *
   * @param targetRange    optimally picks will be within
   * @param voicingNotes   to pick from, at most once each
   * @param seekInversions whether to seek inversions
   */
  public NotePicker(NoteRange targetRange, Collection<Note> voicingNotes, boolean seekInversions) {
    this.targetRange = NoteRange.copyOf(targetRange);
    this.voicingNotes = new HashSet<>(voicingNotes);
    this.voicingRange = NoteRange.ofNotes(voicingNotes);
    this.seekInversions = seekInversions;
  }

  /**
   * Pick all voicing notes for event notes
   */
  public Note pick(Note eventNote) {
    var noteInAvailableOctave = voicingRange.toAvailableOctave(eventNote);

    var picked =
      // if atonal, pick random note
      PitchClass.None.equals(noteInAvailableOctave.getPitchClass()) ? pickRandom(voicingNotes)
        // not atonal, actually pick note
        : voicingNotes.stream()
        .sorted(Note::compareTo)
        .map(vN -> new RankedNote(vN, Math.abs(vN.delta(noteInAvailableOctave))))
        .min(Comparator.comparing(RankedNote::getDelta))
        .map(RankedNote::getTones)
        .map(voicingNote -> seekInversion(voicingNote, targetRange, voicingNotes));

    // Pick the note
    if (picked.isPresent()) {
      // Keep track of the total range of notes selected, to keep voicing in the tightest possible range
      targetRange.expand(picked.get());
      return removePicked(picked.get());
    }

    return Note.of(Note.ATONAL);
  }

  /**
   * @return range of picked notes (updated after picking)
   */
  public NoteRange getTargetRange() {
    return targetRange;
  }

  /**
   * Pick a note, adding it to picked notes and removing it from voicing notes
   *
   * @param picked to pick
   */
  Note removePicked(Note picked) {
    voicingNotes.remove(picked);
    return picked;
  }

  /**
   * Seek the inversion of the given note that is best contained within the given range
   *
   * @param source  for which to seek inversion
   * @param range   towards which seeking will optimize
   * @param options from which to select better notes
   */
  Note seekInversion(Note source, NoteRange range, Collection<Note> options) {
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
        .map(RankedNote::getTones);
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
        .map(RankedNote::getTones);
      if (alt.isPresent()) return alt.get();
    }

    return source;
  }

  /**
   * Pick a random instrument note from the available notes in the voicing
   * <p>
   * Artist writing detail program expects 'X' note value to result in random selection from available Voicings https://www.pivotaltracker.com/story/show/175947230
   *
   * @param voicingNotes to pick from
   * @return a random note from the voicing
   */
  Optional<Note> pickRandom(Collection<Note> voicingNotes) {
    return MarbleBag.quickPick(voicingNotes
      .stream()
      .sorted(Comparator.comparing((s) -> random.nextFloat()))
      .toList());
  }
}
