package io.xj.nexus.craft.arrangement;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import datadog.trace.api.Trace;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioEvent;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.util.CSV;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.FabricationWrapperImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.NameIsometry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Arrangement of Segment Events is a common foundation for both Detail and Rhythm craft
 */
public class ArrangementCraftImpl extends FabricationWrapperImpl {

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  @Inject
  public ArrangementCraftImpl(Fabricator fabricator) {
    super(fabricator);
  }

  /**
   Craft the arrangement for a given voice

   @param choice to craft arrangements for
   @throws NexusException on failure
   */
  protected void craftArrangements(SegmentChoice choice) throws NexusException {
    // this is used to invert voicings into the tightest possible range
    // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
    NoteRange range = new NoteRange();

    var programConfig = fabricator.getProgramConfig(fabricator.getProgram(choice).orElseThrow());

    if (fabricator.getSegmentChords().isEmpty())
      craftArrangementForVoiceSection(choice, 0, fabricator.getSegment().getTotal(), range);

    else if (programConfig.doPatternRestartOnChord())
      craftArrangementForVoiceSectionRestartingEachChord(choice, range);

    else
      craftArrangementForVoiceSection(choice, 0, fabricator.getSegment().getTotal(), range);
  }

  /**
   Iterate through all the chords of a sequence and arrange events per each chord
   <p>
   [#176468993] Detail programs can be made to repeat every chord change

   @param choice from which to craft events
   @param range  used to keep voicing in the tightest range possible
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "craftArrangementForVoiceSectionRestartingEachChord")
  private void craftArrangementForVoiceSectionRestartingEachChord(
    SegmentChoice choice,
    NoteRange range
  ) throws NexusException {
    // guaranteed to be in order of position ascending
    SegmentChord[] chords = new SegmentChord[fabricator.getSegmentChords().size()];
    var i = 0;
    for (var chord : fabricator.getSegmentChords()) {
      chords[i] = chord;
      i++;
    }
    Section[] sections = new Section[chords.length];
    for (i = 0; i < chords.length; i++) {
      sections[i] = new Section();
      sections[i].chord = chords[i];
      sections[i].fromPos = chords[i].getPosition();
      sections[i].toPos = i < chords.length - 1 ?
        chords[i + 1].getPosition() :
        fabricator.getSegment().getTotal();
    }
    for (var section : sections)
      craftArrangementForVoiceSection(choice, section.fromPos, section.toPos, range);
  }

  /**
   Representation of a section of an arrangement, having a chord, beginning position and end position
   */
  static class Section {
    public SegmentChord chord;
    public double fromPos;
    public double toPos;
  }

  /**
   Craft events for a section of one detail voice

   @param choice  from which to craft events
   @param fromPos position (in beats)
   @param maxPos  position (in beats)
   @param range   used to keep voicing in the tightest range possible
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "craftArrangementForVoiceSection")
  private void craftArrangementForVoiceSection(
    SegmentChoice choice,
    double fromPos,
    double maxPos,
    NoteRange range
  ) throws NexusException {
    // choose intro pattern (if available)
    Optional<ProgramSequencePattern> introPattern =
      fabricator.randomlySelectPatternOfSequenceByVoiceAndType(choice, ProgramSequencePattern.Type.Intro);

    // choose outro pattern (if available)
    Optional<ProgramSequencePattern> outroPattern =
      fabricator.randomlySelectPatternOfSequenceByVoiceAndType(choice, ProgramSequencePattern.Type.Outro);

    // compute in and out points, and length # beats for which loop patterns will be required
    double loopOutPos = maxPos - (outroPattern.map(ProgramSequencePattern::getTotal).orElse(0));

    // begin at the beginning and fabricate events for the segment of beginning to end
    double curPos = fromPos;

    // if intro pattern, fabricate those voice event first
    if (introPattern.isPresent())
      curPos += craftPatternEvents(choice, introPattern.get(), curPos, loopOutPos, range);

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < loopOutPos) {
      Optional<ProgramSequencePattern> loopPattern =
        fabricator.randomlySelectPatternOfSequenceByVoiceAndType(choice, ProgramSequencePattern.Type.Loop);
      if (loopPattern.isPresent())
        curPos += craftPatternEvents(choice, loopPattern.get(), curPos, loopOutPos, range);
      else
        curPos = loopOutPos;
    }

    // if outro pattern, fabricate those voice event last
    // [#161466708] compute how much to go for it in the outro
    if (outroPattern.isPresent())
      craftPatternEvents(choice, outroPattern.get(), curPos, maxPos, range);
  }

  /**
   Craft the voice events of a single pattern.
   [#161601279] Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

   @param pattern             to source events
   @param fromSegmentPosition to write events to segment
   @param toSegmentPosition   to write events to segment
   @param range               used to keep voicing in the tightest range possible
   @return deltaPos of start, after crafting this batch of pattern events
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "craftPatternEvents")
  private double craftPatternEvents(
    SegmentChoice choice,
    ProgramSequencePattern pattern,
    double fromSegmentPosition,
    double toSegmentPosition,
    NoteRange range
  ) throws NexusException {
    if (Objects.isNull(pattern)) throw new NexusException("Cannot craft create null pattern");
    double totalBeats = toSegmentPosition - fromSegmentPosition;
    Collection<ProgramSequencePatternEvent> events = fabricator.getSourceMaterial().getEvents(pattern);

    SegmentChoiceArrangement arrangement = fabricator.add(SegmentChoiceArrangement.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(choice.getSegmentId())
      .setSegmentChoiceId(choice.getId())
      .setProgramSequencePatternId(pattern.getId())
      .build());

    var instrument = fabricator.getSourceMaterial().getInstrument(choice.getInstrumentId())
      .orElseThrow(() -> new NexusException("Failed to retrieve instrument"));
    for (ProgramSequencePatternEvent event : events)
      pickNotesAndInstrumentAudioForEvent(instrument, choice, arrangement, event, fromSegmentPosition, toSegmentPosition, range);
    return Math.min(totalBeats, pattern.getTotal());
  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param choice              to pick notes for
   @param event               to pick audio for
   @param fromSegmentPosition to pick notes for
   @param toSegmentPosition   to pick notes for
   @param range               used to keep voicing in the tightest range possible
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "pickInstrumentAudio")
  private void pickNotesAndInstrumentAudioForEvent(
    Instrument instrument,
    SegmentChoice choice,
    SegmentChoiceArrangement arrangement,
    ProgramSequencePatternEvent event,
    Double fromSegmentPosition,
    Double toSegmentPosition,
    NoteRange range
  ) throws NexusException {
    // Morph & Point attributes are expressed in beats
    double segmentPosition = fromSegmentPosition + event.getPosition();
    double duration = Math.min(event.getDuration(), toSegmentPosition - segmentPosition);
    var chord = fabricator.getChordAt(segmentPosition);
    Optional<SegmentChordVoicing> voicing = chord.isPresent() ?
      fabricator.getVoicing(chord.get(), instrument.getType()) :
      Optional.empty();

    // The final note is voiced from the chord voicing (if found) or else the default is used
    Set<String> notes = voicing.isPresent() ?
      pickNotesForEvent(instrument.getType(), choice, event, chord.get(), voicing.get(), range) :
      ImmutableSet.of(Note.ATONAL);

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = fabricator.computeSecondsAtPosition(segmentPosition);
    double lengthSeconds = fabricator.computeSecondsAtPosition(segmentPosition + duration) - startSeconds;

    // pick an audio for each note
    for (var note : notes)
      pickInstrumentAudio(note, instrument, event, arrangement, startSeconds, lengthSeconds,
        voicing.map(SegmentChordVoicing::getId).orElse(null));
  }

  /**
   [#176696738] XJ has a serviceable voicing algorithm
   <p>
   [#176474113] Artist can edit comma-separated notes into detail program events
   <p>
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param note                     to pick audio for
   @param instrument               from which to pick audio
   @param event                    to pick audio for
   @param segmentChoiceArrangement arranging this instrument for a program
   @param startSeconds             of audio
   @param lengthSeconds            of audio
   @throws NexusException on failure
   */
  private void pickInstrumentAudio(
    String note,
    Instrument instrument,
    ProgramSequencePatternEvent event,
    SegmentChoiceArrangement segmentChoiceArrangement,
    double startSeconds,
    double lengthSeconds,
    @Nullable String segmentChordVoicingId
  ) throws NexusException {
    var audio =
      fabricator.getInstrumentConfig(instrument).isMultiphonic() ?
        selectMultiphonicInstrumentAudio(instrument, event, note) :
        selectInstrumentAudio(instrument, event);

    // [#176373977] Should gracefully skip audio in unfulfilled by instrument
    if (audio.isEmpty()) return;

    // of pick
    var builder = SegmentChoiceArrangementPick.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segmentChoiceArrangement.getSegmentId())
      .setSegmentChoiceArrangementId(segmentChoiceArrangement.getId())
      .setInstrumentAudioId(audio.get().getId())
      .setProgramSequencePatternEventId(event.getId())
      .setName(fabricator.getTrackName(event))
      .setStart(startSeconds)
      .setLength(lengthSeconds)
      .setAmplitude(event.getVelocity())
      .setNote(fabricator.getInstrumentConfig(instrument).isTonal() ? note : Note.ATONAL);
    if (Objects.nonNull(segmentChordVoicingId))
      builder.setSegmentChordVoicingId(segmentChordVoicingId);
    fabricator.add(builder.build());
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   [#176695166] XJ should choose correct instrument note based on detail program note

   @param instrumentType comprising audios
   @param choice         for reference
   @param event          of program to pick instrument note for
   @param segmentChord   to use for interpreting the voicing
   @param voicing        to choose a note from
   @param range          used to keep voicing in the tightest range possible
   @return note picked from the available voicing
   */
  private Set<String> pickNotesForEvent(
    Instrument.Type instrumentType,
    SegmentChoice choice,
    ProgramSequencePatternEvent event,
    SegmentChord segmentChord,
    SegmentChordVoicing voicing,
    NoteRange range
  ) throws NexusException {
    var previous = fabricator.getPreviouslyPickedNotes(event.getId(), segmentChord.getName());
    if (previous.isPresent()) return previous.get();

    // Various computations to prepare for picking
    var chord = Chord.of(segmentChord.getName());
    var sourceKey = fabricator.getKeyForChoice(choice);
    var sourceRange = fabricator.computeProgramRange(choice.getProgramId(), instrumentType);
    var targetRange = fabricator.computeVoicingNoteRange(instrumentType);
    var targetShiftSemitones = fabricator.computeTargetShift(sourceKey, Chord.of(chord.getName()));
    var targetShiftOctaves = fabricator.computeRangeShiftOctaves(instrumentType, sourceRange, targetRange);
    var voicingNotes = fabricator.getNotes(voicing).stream().map(Note::of).collect(Collectors.toList());

    var notePicker = new NotePicker(instrumentType, chord, range, voicingNotes,
      CSV.split(event.getNote())
        .stream()
        .map(n -> Note.of(n).shift(targetShiftSemitones + 12 * targetShiftOctaves))
        .collect(Collectors.toList()));

    notePicker.pick();
    range.expand(notePicker.getRange());

    var notes = notePicker.getPickedNotes().stream()
      .map(n -> n.toString(chord.getAdjSymbol())).collect(Collectors.toSet());

    return fabricator.rememberPickedNotes(event.getId(), chord.getName(), notes);
  }

  /**
   Select audio from a multiphonic instrument
   <p>
   [#176649593] Sampler obeys isMultiphonic from Instrument config

   @param instrument of which to score available audios, and make a selection
   @param event      for caching reference
   @param note       to match
   selection)
   @return matched new audio
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectMultiphonicInstrumentAudio")
  private Optional<InstrumentAudio> selectMultiphonicInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event,
    String note
  ) throws NexusException {
    String key = fabricator.keyByTrackNote(event.getProgramVoiceTrackId(), note);

    if (!fabricator.getPreviousInstrumentAudio().containsKey(key)) {
      var audio = selectNewMultiphonicInstrumentAudio(instrument, note);
      if (audio.isPresent()) fabricator.getPreviousInstrumentAudio().put(key, audio.get());
    }

    return fabricator.getPreviousInstrumentAudio().containsKey(key) ?
      Optional.of(fabricator.getPreviousInstrumentAudio().get(key)) : Optional.empty();
  }

  /**
   Select the cached (already selected for this voice + track name)
   instrument audio based on a pattern event.
   <p>
   If never encountered, default to new selection and cache that.

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   selection)
   @return matched new audio
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectInstrumentAudio")
  private Optional<InstrumentAudio> selectInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    String key = fabricator.keyByVoiceTrack(event);

    if (!fabricator.getPreviousInstrumentAudio().containsKey(key)) {
      var audio = selectNewInstrumentAudio(instrument, event);
      if (audio.isPresent()) fabricator.getPreviousInstrumentAudio().put(key, audio.get());
    }

    return fabricator.getPreviousInstrumentAudio().containsKey(key) ?
      Optional.of(fabricator.getPreviousInstrumentAudio().get(key)) : Optional.empty();
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   @return matched new audio
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectNewInstrumentAudio")
  private Optional<InstrumentAudio> selectNewInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    EntityScorePicker<InstrumentAudio> audioEntityScorePicker = new EntityScorePicker<>();

    // add all audio to chooser
    audioEntityScorePicker.addAll(fabricator.getSourceMaterial().getAudios(instrument));

    // score each audio against the current voice event, with some variability
    for (InstrumentAudioEvent audioEvent : fabricator.getSourceMaterial().getFirstEventsOfAudiosOfInstrument(instrument))
      switch (instrument.getType()) {

        case Percussive:
          audioEntityScorePicker.score(audioEvent.getInstrumentAudioId(),
            NameIsometry.similarity(fabricator.getTrackName(event), audioEvent.getName()));
          break;

        case Bass:
        case Pad:
        case Sticky:
        case Stripe:
        case Stab:
        default:
          audioEntityScorePicker.score(audioEvent.getInstrumentAudioId(),
            Note.of(audioEvent.getNote()).sameAs(Note.of(event.getNote())) ? 100.0 : 0.0);
          break;
      }


    // final chosen audio event
    return audioEntityScorePicker.getTop();
  }

  /**
   Select a new random instrument audio based on a pattern event
   <p>
   [#176649593] Sampler obeys isMultiphonic from Instrument config

   @param instrument of which to score available audios, and make a selection
   @param note       to match
   @return matched new audio
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectNewMultiphonicInstrumentAudio")
  private Optional<InstrumentAudio> selectNewMultiphonicInstrumentAudio(
    Instrument instrument,
    String note
  ) throws NexusException {
    var audioEvent = fabricator.getFirstEventsOfAudiosOfInstrument(instrument)
      .stream()
      .filter(instrumentAudioEvent -> Note.of(instrumentAudioEvent.getNote()).sameAs(Note.of(note)))
      .findAny();

    if (audioEvent.isEmpty()) {
      reportMissing(ImmutableMap.of(
        "instrumentId", instrument.getId(),
        "searchForNote", note,
        "availableNotes", CSV.from(fabricator.getFirstEventsOfAudiosOfInstrument(instrument)
          .stream()
          .map(InstrumentAudioEvent::getNote)
          .map(Note::of)
          .sorted(Note::compareTo)
          .map(n -> n.toString(AdjSymbol.Sharp))
          .collect(Collectors.toList()))
      ));
      return Optional.empty();
    }

    return fabricator.getSourceMaterial().getInstrumentAudio(audioEvent.get().getInstrumentAudioId());
  }
}
