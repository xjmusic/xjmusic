package io.xj.nexus.craft.arrangement;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.Segment;
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
import io.xj.lib.util.Chance;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.FabricationWrapperImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.NameIsometry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Arrangement of Segment Events is a common foundation for both Detail and Rhythm craft
 */
public class ArrangementCraftImpl extends FabricationWrapperImpl {
  protected static final double SCORE_DIRECTLY_BOUND = 100;
  protected static final double SCORE_ENTROPY_CHOICE_DETAIL = 8.0;
  protected static final double SCORE_ENTROPY_CHOICE_INSTRUMENT = 8.0;
  protected static final double SCORE_ENTROPY_CHOICE_RHYTHM = 8.0;
  protected static final double SCORE_MATCHED_MEMES = 1.0;
  protected static final double SCORE_MATCHED_MAIN_PROGRAM = 10;
  private static final List<Instrument.Type> DETAIL_INSTRUMENT_TYPES = new ArrayList<>(ImmutableList.of(
    Instrument.Type.Bass,
    Instrument.Type.Stripe,
    Instrument.Type.Pad,
    Instrument.Type.Sticky,
    Instrument.Type.Stab
  ));
  private static final List<Segment.Type> PRECOMPUTE_FOR_TYPES = ImmutableList.of(
    Segment.Type.Initial,
    Segment.Type.NextMain,
    Segment.Type.NextMacro
  );
  private String rhythmIntroVoiceName;
  private String rhythmOutroVoiceName;
  private Instrument.Type detailIntroVoiceType;
  private Instrument.Type detailOutroVoiceType;
  private final Map<String, Integer> deltaRhythmIns = Maps.newHashMap();
  private final Map<String, Integer> deltaRhythmOuts = Maps.newHashMap();
  private final Map<String, Integer> deltaDetailIns = Maps.newHashMap();
  private final Map<String, Integer> deltaDetailOuts = Maps.newHashMap();

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  @Inject
  public ArrangementCraftImpl(Fabricator fabricator) {
    super(fabricator);
    deltaRhythmIns.clear();
    deltaRhythmOuts.clear();
    deltaDetailIns.clear();
    deltaDetailOuts.clear();
  }

  /**
   Segments have intensity arcs; automate mixer layers in and out of each main program
   https://www.pivotaltracker.com/story/show/178240332

   @param sequence           for which to craft choices
   @param voices             for which to craft choices
   @param instrumentProvider from which to get instruments
   @throws NexusException on failure
   */
  protected void craftChoices(ProgramSequence sequence, Collection<ProgramVoice> voices, InstrumentProvider instrumentProvider) throws NexusException {
    // Craft each voice into choice
    for (ProgramVoice voice : voices) {
      var choiceBuilder = SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setProgramType(fabricator.sourceMaterial().getProgram(voice.getProgramId())
          .orElseThrow(() -> new NexusException("Can't get program for voice")).getType())
        .setInstrumentType(voice.getType())
        .setProgramId(voice.getProgramId())
        .setProgramSequenceId(sequence.getId())
        .setProgramVoiceId(voice.getId())
        .setSegmentId(fabricator.getSegment().getId());

      // Whether there is a prior choice for this voice
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceOfSameMainProgram(voice);

      if (priorChoice.isPresent()) {
        this.craftArrangements(fabricator.add(choiceBuilder
          .setDeltaIn(priorChoice.get().getDeltaIn())
          .setDeltaOut(priorChoice.get().getDeltaOut())
          .setInstrumentId(priorChoice.get().getInstrumentId())
          .build()));
        continue;
      }

      var instrument = instrumentProvider.get(voice);
      if (instrument.isEmpty()) {
        reportMissing(Instrument.class, String.format("%s-type instrument", voice.getType()));
        continue;
      }

      // make new choices
      this.craftArrangements(fabricator.add(choiceBuilder
        .setDeltaIn(isIntro(voice) ? Segments.DELTA_UNLIMITED : computeDeltaIn(voice))
        .setDeltaOut(isOutro(voice) ? Segments.DELTA_UNLIMITED : computeDeltaOut(voice))
        .setInstrumentId(instrument.get().getId())
        .build()));
    }
  }

  /**
   Craft the arrangement for a given voice
   <p>
   Choice inertia
   https://www.pivotaltracker.com/story/show/178442889
   Perform the inertia analysis, and determine whether the actually use the new choice or not
   IMPORTANT** If the previously chosen instruments are for the previous main program as the current segment,
   the inertia scores are not actually added to the regular scores or used to make choices--
   this would prevent new choices from being made. **Inertia must be its own layer of calculation,
   a question of whether the choices will be followed or whether the inertia will be followed**
   thus the new choices have been made, we know *where* we're going next,
   but we aren't actually using them yet until we hit the next main program in full, N segments later.

   @param choice to craft arrangements for
   @throws NexusException on failure
   */
  protected void craftArrangements(SegmentChoice choice) throws NexusException {
    // this is used to invert voicings into the tightest possible range
    // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
    NoteRange range = new NoteRange();

    var programConfig = fabricator.getProgramConfig(fabricator.getProgram(choice)
      .orElseThrow(() -> new NexusException("Can't get program config")));

    if (fabricator.getSegmentChords().isEmpty())
      craftArrangementForVoiceSection(choice, 0, fabricator.getSegment().getTotal(), range);

    else if (programConfig.doPatternRestartOnChord())
      craftArrangementForVoiceSectionRestartingEachChord(choice, range);

    else
      craftArrangementForVoiceSection(choice, 0, fabricator.getSegment().getTotal(), range);
  }

  /**
   Precompute all deltas for a given program

   @throws NexusException on failure
   */
  protected void precomputeRhythmDeltas(String programId) throws NexusException {
    if (!PRECOMPUTE_FOR_TYPES.contains(fabricator.getType())) return;

    var limit = fabricator.getChainConfig().getMainProgramLengthMaxDelta();
    var voices = fabricator.sourceMaterial().getAllProgramVoices()
      .stream().filter(candidate -> programId.equals(candidate.getProgramId()))
      .collect(Collectors.toList());
    double unit = (double) (limit / 3) / voices.size();

    Collections.shuffle(voices);
    for (int i = 0; i < voices.size(); i++)
      deltaRhythmIns.put(voices.get(i).getId(), (int) Chance.normallyAround((i + 1) * unit, unit));
    fabricator.addMessageInfo(String.format("Computed Rhythm Delta In: %s", csv(deltaRhythmIns)));

    Collections.shuffle(voices);
    for (int i = 0; i < voices.size(); i++)
      deltaRhythmOuts.put(voices.get(i).getId(), (int) Chance.normallyAround((double) (2 * limit / 3) + (i + 1) * unit, unit / 3));
    fabricator.addMessageInfo(String.format("Computed Rhythm Delta Out: %s", csv(deltaRhythmIns)));
  }

  /**
   Precompute all deltas for a given program

   @throws NexusException on failure
   */
  protected void precomputeDetailDeltas() throws NexusException {
    if (!PRECOMPUTE_FOR_TYPES.contains(fabricator.getType())) return;

    var limit = fabricator.getChainConfig().getMainProgramLengthMaxDelta();
    double unit = (double) (limit / 2) / DETAIL_INSTRUMENT_TYPES.size();

    Collections.shuffle(DETAIL_INSTRUMENT_TYPES);
    for (int i = 0; i < DETAIL_INSTRUMENT_TYPES.size(); i++)
      deltaDetailIns.put(DETAIL_INSTRUMENT_TYPES.get(i).toString(), (int) Chance.normallyAround((i + 1) * unit, unit / 2));
    fabricator.addMessageInfo(String.format("Computed Detail Delta In: %s", csv(deltaDetailIns)));

    Collections.shuffle(DETAIL_INSTRUMENT_TYPES);
    for (int i = 0; i < DETAIL_INSTRUMENT_TYPES.size(); i++)
      deltaDetailOuts.put(DETAIL_INSTRUMENT_TYPES.get(i).toString(), (int) Chance.normallyAround((double) (limit / 2) + (i + 1) * unit, unit / 2));
    fabricator.addMessageInfo(String.format("Computed Detail Delta Out: %s", csv(deltaDetailIns)));
  }

  /**
   Iterate through all the chords of a sequence and arrange events per each chord
   <p>
   [#176468993] Detail programs can be made to repeat every chord change

   @param choice from which to craft events
   @param range  used to keep voicing in the tightest range possible
   @throws NexusException on failure
   */
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
   Craft events for a section of one detail voice

   @param choice  from which to craft events
   @param fromPos position (in beats)
   @param maxPos  position (in beats)
   @param range   used to keep voicing in the tightest range possible
   @throws NexusException on failure
   */
  private void craftArrangementForVoiceSection(
    SegmentChoice choice,
    double fromPos,
    double maxPos,
    NoteRange range
  ) throws NexusException {
    // choose intro pattern (if available)
    Optional<ProgramSequencePattern> introPattern =
      fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice, ProgramSequencePattern.Type.Intro);

    // choose outro pattern (if available)
    Optional<ProgramSequencePattern> outroPattern =
      fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice, ProgramSequencePattern.Type.Outro);

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
        fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice, ProgramSequencePattern.Type.Loop);
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
  private double craftPatternEvents(
    SegmentChoice choice,
    ProgramSequencePattern pattern,
    double fromSegmentPosition,
    double toSegmentPosition,
    NoteRange range
  ) throws NexusException {
    if (Objects.isNull(pattern)) throw new NexusException("Cannot craft create null pattern");
    double totalBeats = toSegmentPosition - fromSegmentPosition;
    Collection<ProgramSequencePatternEvent> events = fabricator.sourceMaterial().getEvents(pattern);

    SegmentChoiceArrangement arrangement = fabricator.add(SegmentChoiceArrangement.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(choice.getSegmentId())
      .setSegmentChoiceId(choice.getId())
      .setProgramSequencePatternId(pattern.getId())
      .build());

    var instrument = fabricator.sourceMaterial().getInstrument(choice.getInstrumentId())
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

    // [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program
    if (Segments.DELTA_UNLIMITED != choice.getDeltaIn() && fabricator.getSegment().getDelta() + segmentPosition < choice.getDeltaIn())
      return;

    // layer is not playing this whole segment
    if (Segments.DELTA_UNLIMITED != choice.getDeltaOut() && choice.getDeltaOut() < fabricator.getSegment().getDelta())
      return;

    // layer is fading out during this segment
    var volRatio = Segments.DELTA_UNLIMITED != choice.getDeltaOut() && fabricator.getSegment().getDelta() + segmentPosition > choice.getDeltaOut() ?
      1.0 - (segmentPosition - (choice.getDeltaOut() - fabricator.getSegment().getDelta())) / (fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal() - choice.getDeltaOut()) : 1.0;

    // The final note is voiced from the chord voicing (if found) or else the default is used
    Set<String> notes = voicing.isPresent() ?
      pickNotesForEvent(instrument.getType(), choice, event, chord.get(), voicing.get(), range) :
      ImmutableSet.of(Note.ATONAL);

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = fabricator.getSecondsAtPosition(segmentPosition);
    double lengthSeconds = fabricator.getSecondsAtPosition(segmentPosition + duration) - startSeconds;

    // pick an audio for each note
    for (var note : notes)
      pickInstrumentAudio(note, instrument, event, arrangement, startSeconds, lengthSeconds,
        voicing.map(SegmentChordVoicing::getId).orElse(null), volRatio);
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
   @param volRatio                 ratio of volume
   @throws NexusException on failure
   */
  private void pickInstrumentAudio(
    String note,
    Instrument instrument,
    ProgramSequencePatternEvent event,
    SegmentChoiceArrangement segmentChoiceArrangement,
    double startSeconds,
    double lengthSeconds,
    @Nullable String segmentChordVoicingId,
    double volRatio
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
      .setAmplitude(event.getVelocity() * volRatio)
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
    var previous = fabricator.getPreferredNotes(event.getId(), segmentChord.getName());
    if (previous.isPresent()) return previous.get();

    // Various computations to prepare for picking
    var chord = Chord.of(segmentChord.getName());
    var sourceKey = fabricator.getKeyForChoice(choice);
    var sourceRange = fabricator.getProgramRange(choice.getProgramId(), instrumentType);
    var targetRange = fabricator.getProgramVoicingNoteRange(instrumentType);
    var targetShiftSemitones = fabricator.getProgramTargetShift(sourceKey, Chord.of(chord.getName()));
    var targetShiftOctaves = fabricator.getProgramRangeShiftOctaves(instrumentType, sourceRange, targetRange);
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
   */
  private Optional<InstrumentAudio> selectMultiphonicInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event,
    String note
  ) {
    if (fabricator.getPreferredAudio(event, note).isEmpty()) {
      var audio = selectNewMultiphonicInstrumentAudio(instrument, note);
      audio.ifPresent(instrumentAudio -> fabricator.setPreferredAudio(event, note, instrumentAudio));
    }

    return fabricator.getPreferredAudio(event, note);
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
  private Optional<InstrumentAudio> selectInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    if (fabricator.getPreferredAudio(event, event.getNote()).isEmpty()) {
      var audio = selectNewInstrumentAudio(instrument, event);
      audio.ifPresent(instrumentAudio -> fabricator.setPreferredAudio(event, event.getNote(), instrumentAudio));
    }

    return fabricator.getPreferredAudio(event, event.getNote());
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   @return matched new audio
   */
  private Optional<InstrumentAudio> selectNewInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    EntityScorePicker<InstrumentAudio> audioEntityScorePicker = new EntityScorePicker<>();

    // add all audio to chooser
    audioEntityScorePicker.addAll(fabricator.sourceMaterial().getAudios(instrument));

    // score each audio against the current voice event, with some variability
    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudios(instrument))
      if (instrument.getType() == Instrument.Type.Percussive)
        audioEntityScorePicker.score(audio.getId(), NameIsometry.similarity(fabricator.getTrackName(event), audio.getEvent()));
      else
        audioEntityScorePicker.score(audio.getId(), Note.of(audio.getNote()).sameAs(Note.of(event.getNote())) ? 100.0 : 0.0);

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
  private Optional<InstrumentAudio> selectNewMultiphonicInstrumentAudio(
    Instrument instrument,
    String note
  ) {
    var instrumentAudios = fabricator.sourceMaterial().getAudios(instrument);
    var audio = instrumentAudios
      .stream()
      .filter(A -> Note.of(A.getNote()).sameAs(Note.of(note)))
      .findAny();

    if (audio.isEmpty()) {
      reportMissing(ImmutableMap.of(
        "instrumentId", instrument.getId(),
        "searchForNote", note,
        "availableNotes", CSV.from(instrumentAudios
          .stream()
          .map(InstrumentAudio::getNote)
          .map(Note::of)
          .sorted(Note::compareTo)
          .map(N -> N.toString(AdjSymbol.Sharp))
          .collect(Collectors.toList()))
      ));
      return Optional.empty();
    }

    return fabricator.sourceMaterial().getInstrumentAudio(audio.get().getId());
  }

  /**
   Determine whether the given voice is the featured Intro of the segment, based on:
   - for an Initial-type segment, chosen at random
   - for a Continue-type segment, carry over choices that were featured in the previous segment
   - for a NextMain/NextMacro-type segment, carry over from the outro of the previous segment

   @param voice for which to make determination
   @return true if this voice is the Intro
   */
  private boolean isIntro(ProgramVoice voice) throws NexusException {
    var programType = fabricator.getProgramType(voice);
    return switch (programType) {
      case UNRECOGNIZED, Macro, Main -> false;
      case Rhythm -> {
        if (Objects.isNull(rhythmIntroVoiceName))
          findIntroChoice(programType, voice)
            .flatMap(c -> fabricator.sourceMaterial().getProgramVoice(c.getProgramVoiceId()))
            .ifPresent(v -> rhythmIntroVoiceName = v.getName());
        if (Objects.isNull(rhythmIntroVoiceName))
          rhythmIntroVoiceName = fabricator.getRandomlySelectedVoiceForProgramId(voice.getProgramId(), ImmutableList.of())
            .orElseThrow(() -> new NexusException("Failed to randomly select voice!"))
            .getName();
        yield rhythmIntroVoiceName.equals(voice.getName());
      }
      case Detail -> {
        if (Objects.isNull(detailIntroVoiceType))
          findIntroChoice(programType, voice)
            .ifPresent(c -> detailIntroVoiceType = c.getInstrumentType());
        if (Objects.isNull(detailIntroVoiceType))
          detailIntroVoiceType = selectRandomDetailInstrumentType(ImmutableList.of())
            .orElseThrow(() -> new NexusException("Failed to randomly select instrument type!"));
        yield detailIntroVoiceType.equals(voice.getType());
      }
    };
  }

  /**
   Determine whether the given voice is the featured Outro of the segment, based on:
   - for an Initial-type segment, chosen at random
   - for a NextMain/NextMacro/Continue-type segment, carry over choices that were featured in the previous segment

   @param voice for which to make determination
   @return true if this voice is the Outro
   */
  private boolean isOutro(ProgramVoice voice) throws NexusException {
    var programType = fabricator.getProgramType(voice);
    return switch (programType) {
      case UNRECOGNIZED, Macro, Main -> false;
      case Rhythm -> {
        if (Objects.isNull(rhythmOutroVoiceName))
          findOutroChoice(programType, voice)
            .flatMap(c -> fabricator.sourceMaterial().getProgramVoice(c.getProgramVoiceId()))
            .ifPresent(v -> rhythmOutroVoiceName = v.getName());
        if (Objects.isNull(rhythmOutroVoiceName))
          rhythmOutroVoiceName = fabricator.getRandomlySelectedVoiceForProgramId(voice.getProgramId(), ImmutableList.of(rhythmIntroVoiceName))
            .orElseThrow(() -> new NexusException("Failed to randomly select voice!"))
            .getName();
        yield rhythmOutroVoiceName.equals(voice.getName());
      }
      case Detail -> {
        if (Objects.isNull(detailOutroVoiceType))
          findOutroChoice(programType, voice)
            .ifPresent(c -> detailOutroVoiceType = c.getInstrumentType());
        if (Objects.isNull(detailOutroVoiceType))
          detailOutroVoiceType = selectRandomDetailInstrumentType(ImmutableList.of(detailIntroVoiceType))
            .orElseThrow(() -> new NexusException("Failed to randomly select instrument type!"));
        yield detailOutroVoiceType.equals(voice.getType());
      }
    };
  }

  /**
   Compute the intro for a given voice

   @param programType for which to compute intro
   @param voice       for which to get intro
   @return intro
   @throws NexusException on failure
   */
  private Optional<SegmentChoice> findIntroChoice(Program.Type programType, ProgramVoice voice) throws NexusException {
    return switch (fabricator.getType()) {
      case UNRECOGNIZED, Pending, Initial -> Optional.empty();
      case Continue -> fabricator.retrospective().getChoices().stream()
        .filter(choice -> Segments.DELTA_UNLIMITED == choice.getDeltaIn()
          && choice.getProgramType().equals(programType)
          && choice.getInstrumentType().equals(voice.getType()))
        .findFirst();
      case NextMain, NextMacro -> fabricator.retrospective().getChoices().stream()
        .filter(choice -> Segments.DELTA_UNLIMITED == choice.getDeltaOut()
          && choice.getProgramType().equals(programType)
          && choice.getInstrumentType().equals(voice.getType()))
        .findFirst();
    };
  }

  /**
   Compute the outro for a given voice

   @param programType for which to get outro
   @param voice       for which to get outro
   @return outro
   @throws NexusException on failure
   */
  private Optional<SegmentChoice> findOutroChoice(Program.Type programType, ProgramVoice voice) throws NexusException {
    return switch (fabricator.getType()) {
      case UNRECOGNIZED, Pending, Initial, NextMain, NextMacro -> Optional.empty();
      case Continue -> fabricator.retrospective().getChoices().stream()
        .filter(choice -> Segments.DELTA_UNLIMITED == choice.getDeltaOut()
          && choice.getProgramType().equals(programType)
          && choice.getInstrumentType().equals(voice.getType()))
        .findFirst();
    };
  }

  /**
   Select a detail instrument type at random

   @param excludeTypes to exclude from random selection
   @return instrument type
   */
  private Optional<Instrument.Type> selectRandomDetailInstrumentType(Collection<Instrument.Type> excludeTypes) {
    EntityScorePicker<Instrument.Type> entityScorePicker = new EntityScorePicker<>();
    DETAIL_INSTRUMENT_TYPES.stream()
      .filter(type -> !excludeTypes.contains(type))
      .forEach(entityScorePicker::add);

    return entityScorePicker.getTop();
  }

  /**
   Compute the rhythm delta out

   @param voice for which to make determination
   @return true if this voice is the Intro
   */
  private int computeDeltaOut(ProgramVoice voice) throws NexusException {
    return switch (fabricator.getProgramType(voice)) {
      case UNRECOGNIZED, Macro, Main -> Segments.DELTA_UNLIMITED;
      case Detail -> computeDelta(voice, deltaDetailOuts);
      case Rhythm -> computeDelta(voice, deltaRhythmOuts);
    };
  }

  /**
   Compute the rhythm delta in

   @param voice for which to make determination
   @return true if this voice is the Intro
   */
  private int computeDeltaIn(ProgramVoice voice) throws NexusException {
    return switch (fabricator.getProgramType(voice)) {
      case UNRECOGNIZED, Macro, Main -> Segments.DELTA_UNLIMITED;
      case Detail -> computeDelta(voice, deltaDetailIns);
      case Rhythm -> computeDelta(voice, deltaRhythmIns);
    };
  }

  /**
   Compute the delta out for the given voice.
   The thing here is to stagger the delta outs,
   with some randomness, but essentially spread even throughout
   the second half (delta outs) of any given main program.

   @param voice for which to make determination
   @return true if this voice is the Intro
   */
  private int computeDelta(ProgramVoice voice, Map<String, Integer> map) {
    if (!fabricator.getChainConfig().isChoiceDeltaEnabled()) return Segments.DELTA_UNLIMITED;
    return map.getOrDefault(voice.getType().toString(), Segments.DELTA_UNLIMITED);
  }

  /**
   Get a CSV of the map like "key@value, key@value"

   @param map for which to get CSV string
   @return CSV string
   */
  private String csv(Map<String, Integer> map) {
    return map.entrySet().stream()
      .map(di -> String.format("%s@%s", di.getKey(), di.getValue()))
      .collect(Collectors.joining(", "));
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
   Instrument provider to make some code more portable
   */
  public interface InstrumentProvider {
    Optional<Instrument> get(ProgramVoice voice) throws NexusException;
  }
}
