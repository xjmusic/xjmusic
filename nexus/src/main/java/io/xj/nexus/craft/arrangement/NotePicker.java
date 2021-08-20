package io.xj.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import io.xj.api.InstrumentType;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 In order to pick exactly one optimal voicing note for each of the source event notes.
 */
public class NotePicker {
  private static final int WEIGHT_MATCH_SLASH_ROOT = 10;
  private final InstrumentType instrumentType;
  private final Chord chord;
  private final NoteRange range;
  private final Set<Note> eventNotes;
  private final Set<Note> voicingNotes;
  private final Set<Note> pickedNotes;
  private final SecureRandom random = new SecureRandom();
  private final Collection<InstrumentType> instrumentTypesToSeekInversions = ImmutableList.of(
    InstrumentType.STRIPE,
    InstrumentType.STAB,
    InstrumentType.PAD,
    InstrumentType.STICKY
  );

  /**
   Build a NotePicker from the given optimal target range,@param instrumentType for which to pick notes

   @param chord        for prioritizing notes especially
   @param range        optimally picks will be within
   @param voicingNotes to pick from, at most once each
   @param eventNotes   for which to pick exactly one voicing note each
   */
  public NotePicker(InstrumentType instrumentType, Chord chord, NoteRange range, Collection<Note> voicingNotes, Collection<Note> eventNotes) {
    this.instrumentType = instrumentType;
    this.chord = chord;
    this.range = NoteRange.copyOf(range);
    this.eventNotes = new HashSet<>(eventNotes);
    this.voicingNotes = new HashSet<>(voicingNotes);
    pickedNotes = new HashSet<>();
  }

  /**
   Pick all voicing notes for event notes
   */
  public void pick() {
    // Pick the notes
    for (var eN : eventNotes.stream().sorted(Note::compareTo).collect(Collectors.toList())) {
      if (PitchClass.None.equals(eN.getPitchClass()))
        pickRandom(voicingNotes)
          .ifPresent(this::pick);
      else
        voicingNotes.stream()
          .sorted(Note::compareTo)
          .map(vN -> new RankedNote(vN,
            Math.abs(vN.delta(eN))
              - weightIfMatchSlashRoot(instrumentType, vN, chord)))
          .min(Comparator.comparing(RankedNote::getDelta))
          .map(RankedNote::getNote)
          .map(voicingNote -> seekInversion(voicingNote, range, voicingNotes))
          .ifPresent(this::pick);
    }

    // If nothing has made it through to here, pick a single atonal note.
    if (pickedNotes.isEmpty()) pickedNotes.add(Note.of(Note.ATONAL));

    // Keep track of the total range of notes selected, to keep voicing in tightest possible range
    range.expand(pickedNotes);
  }

  /**
   @return range of picked notes (updated after picking)
   */
  public NoteRange getRange() {
    return range;
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
    if (!instrumentTypesToSeekInversions.contains(instrumentType)) return source;

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

  /**
   Weight a note if it matches the slash chord root

   @param instrumentType mode of weighing
   @param note           to weigh
   @param chord          to weigh against
   @return weight
   */
  @SuppressWarnings("SwitchStatementWithTooFewBranches")
  private int weightIfMatchSlashRoot(InstrumentType instrumentType, Note note, Chord chord) {
    return switch (instrumentType) {
      case BASS -> chord.getSlashRoot().equals(note.getPitchClass()) ? WEIGHT_MATCH_SLASH_ROOT : 0;
      default -> 0;
    };
  }


}
