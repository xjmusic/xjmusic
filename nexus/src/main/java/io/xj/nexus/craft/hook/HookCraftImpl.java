// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.hook;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.util.MarbleBag;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.craft.NotePicker;
import io.xj.nexus.fabricator.Fabricator;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class HookCraftImpl extends CraftImpl implements HookCraft {
  @Inject
  public HookCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    if (!fabricator.sourceMaterial().hasInstruments(InstrumentType.Hook)) return;

    // Instrument is from prior choice, else freshly chosen
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(InstrumentType.Hook);

    // Instruments may be chosen without programs
    // https://www.pivotaltracker.com/story/show/181290857
    Optional<Instrument> instrument = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) :
      chooseFreshInstrument(InstrumentType.Hook, List.of(), null, List.of());

    // VoicingLoop instrument mode
    // https://www.pivotaltracker.com/story/show/181815619
    if (instrument.isPresent() && InstrumentMode.VoicingLoop.equals(instrument.get().getMode()))
      craftHook(instrument.get().getId());

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Craft percussion loop

   @param instrumentId of percussion loop instrument to craft
   */
  @SuppressWarnings("DuplicatedCode")
  private void craftHook(UUID instrumentId) throws NexusException {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setInstrumentType(InstrumentType.Hook.toString());
    choice.setInstrumentId(instrumentId);
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Attempt to keep the range in the center
    var targetRange = fabricator.getInstrumentRange(choice.getInstrumentId());

    // Start at zero and keep laying down hook loops until we're out of here
    double pos = 0;
    while (pos < fabricator.getSegment().getTotal()) {
      var audio = selectAudioForInstrument(choice, pos, targetRange);

      // https://www.pivotaltracker.com/story/show/176373977 Should gracefully skip audio in unfulfilled by instrument
      if (audio.isEmpty()) return;

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.getSecondsAtPosition(pos);
      double lengthSeconds = fabricator.getSecondsAtPosition(pos + audio.get().getTotalBeats()) - startSeconds;

      // of pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStart(startSeconds);
      pick.setLength(lengthSeconds);
      pick.setAmplitude(1.0);
      pick.setEvent("HOOK");
      pick.setInstrumentAudioId(audio.get().getId());
      fabricator.put(pick);

      pos += audio.get().getTotalBeats();
    }
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @return drum-type Instrument
   */
  private Optional<InstrumentAudio> selectAudioForInstrument(
    SegmentChoice choice,
    double segmentPosition,
    NoteRange targetRange
  ) throws NexusException {
    var segmentChord = fabricator.getChordAt(segmentPosition);
    if (segmentChord.isEmpty()) return Optional.empty();

    var voicing = fabricator.getVoicing(segmentChord.get(), InstrumentType.Hook);
    if (voicing.isEmpty()) return Optional.empty();

    var note = selectNote(segmentChord.get(), voicing.get(), targetRange);
    if (note.isEmpty()) return Optional.empty();

    if (fabricator.getPreferredAudio(InstrumentType.Hook.toString(), segmentChord.toString()).isEmpty())
      selectNewInstrumentAudio(choice, note.get()).ifPresent(instrumentAudio ->
        fabricator.putPreferredAudio(InstrumentType.Hook.toString(), segmentChord.toString(), instrumentAudio));

    return fabricator.getPreferredAudio(InstrumentType.Hook.toString(), segmentChord.toString());
  }

  /**
   Select a new random instrument audio

   @param choice of which to score available audios, and make a selection
   @param note   to match
   @return matched new audio
   */
  private Optional<InstrumentAudio> selectNewInstrumentAudio(
    SegmentChoice choice,
    Note note
  ) {
    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(choice.getInstrumentId()))
      if (Note.of(audio.getNote()).sameAs(note))
        bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

  /**
   Pick final note based on transposition and current chord

   @param segmentChord to use for interpreting the voicing
   @param voicing      to choose a note from
   @param range        used to keep voicing in the tightest range possible
   @return note picked from the available voicing
   */
  private Optional<Note> selectNote(
    SegmentChord segmentChord,
    SegmentChordVoicing voicing,
    NoteRange range
  ) throws NexusException {
    var previous = fabricator.getPreferredNotes(InstrumentType.Hook.toString(), segmentChord.getName());
    if (previous.isPresent()) return previous.get().stream().map(Note::of).findFirst();

    // Various computations to prepare for picking
    var chord = Chord.of(segmentChord.getName());
    var voicingNotes = fabricator.getNotes(voicing).stream()
      .flatMap(Note::ofValid)
      .collect(Collectors.toList());

    var notePicker = new NotePicker(range, voicingNotes, range.getMedianNote().stream().toList(), true);

    notePicker.pick();
    range.expand(notePicker.getTargetRange());

    var notes = notePicker.getPickedNotes().stream()
      .map(n -> n.toString(chord.getAdjSymbol())).collect(Collectors.toSet());

    fabricator.putPreferredNotes(InstrumentType.Hook.toString(), segmentChord.getName(), notes);

    return notes.stream().map(Note::of).findFirst();
  }


}
