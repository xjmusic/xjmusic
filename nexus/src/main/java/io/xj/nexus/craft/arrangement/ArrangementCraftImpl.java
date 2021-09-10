package io.xj.nexus.craft.arrangement;

import com.google.api.client.util.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentType;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramVoice;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Chance;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.FabricationWrapperImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.NameIsometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.xj.nexus.dao.Segments.DELTA_UNLIMITED;

/**
 Arrangement of Segment Events is a common foundation for both Detail and Rhythm craft
 */
public class ArrangementCraftImpl extends FabricationWrapperImpl {
  private static final double DELTA_SHIFT_FRONTEND_RATIO = 0.62;
  private final Logger LOG = LoggerFactory.getLogger(ArrangementCraftImpl.class);
  private final Map<String, Integer> deltaIns = Maps.newHashMap();
  private final Map<String, Integer> deltaOuts = Maps.newHashMap();
  private ChoiceIndexProvider choiceIndexProvider = new DefaultChoiceIndexProvider();

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  @Inject
  public ArrangementCraftImpl(Fabricator fabricator) {
    super(fabricator);
  }

  /**
   Segments have intensity arcs; automate mixer layers in and out of each main program
   https://www.pivotaltracker.com/story/show/178240332

   @param sequence           for which to craft choices
   @param voices             for which to craft choices
   @param instrumentProvider from which to get instruments
   @param defaultAtonal      whether to default to a single atonal note, if no voicings are available
   @throws NexusException on failure
   */
  protected void craftChoices(ProgramSequence sequence, Collection<ProgramVoice> voices, InstrumentProvider instrumentProvider, boolean defaultAtonal) throws NexusException {
    // Craft each voice into choice
    for (ProgramVoice voice : voices) {
      var choiceBuilder = new SegmentChoice()
        .id(UUID.randomUUID())
        .programType(fabricator.sourceMaterial().getProgram(voice.getProgramId())
          .orElseThrow(() -> new NexusException("Can't get program for voice")).getType())
        .instrumentType(voice.getType())
        .programId(voice.getProgramId())
        .programSequenceId(sequence.getId())
        .programVoiceId(voice.getId())
        .segmentId(fabricator.getSegment().getId());

      // Whether there is a prior choice for this voice
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(voice);

      if (priorChoice.isPresent()) {
        this.craftArrangements(fabricator.add(choiceBuilder
          .deltaIn(priorChoice.get().getDeltaIn())
          .deltaOut(priorChoice.get().getDeltaOut())
          .instrumentId(priorChoice.get().getInstrumentId())
        ), defaultAtonal);
        continue;
      }

      var instrument = instrumentProvider.get(voice);
      if (instrument.isEmpty()) {
        reportMissingInstrumentAudio(Instrument.class, String.format("%s-type instrument", voice.getType()));
        continue;
      }

      // make new choices
      this.craftArrangements(fabricator.add(choiceBuilder
        .deltaIn(getDeltaIn(choiceBuilder))
        .deltaOut(getDeltaOut(choiceBuilder))
        .instrumentId(instrument.get().getId())
      ), defaultAtonal);
    }
  }

  /**
   Get the delta in for the given voice

   @param choice for which to get delta in
   @return delta in for given voice
   */
  protected int getDeltaIn(SegmentChoice choice) {
    try {
      return deltaIns.getOrDefault(choiceIndexProvider.get(choice), DELTA_UNLIMITED);
    } catch (NexusException e) {
      return DELTA_UNLIMITED;
    }
  }

  /**
   Get the delta out for the given voice

   @param choice for which to get delta out
   @return delta out for given voice
   */
  protected int getDeltaOut(SegmentChoice choice) {
    try {
      return deltaOuts.getOrDefault(choiceIndexProvider.get(choice), DELTA_UNLIMITED);
    } catch (NexusException e) {
      return DELTA_UNLIMITED;
    }
  }

  /**
   Craft the arrangement for a given voice
   <p>
   Choice inertia
   https://www.pivotaltracker.com/story/show/178442889
   Perform the inertia analysis, and determine whether they actually use the new choice or not
   IMPORTANT** If the previously chosen instruments are for the previous main program as the current segment,
   the inertia scores are not actually added to the regular scores or used to make choices--
   this would prevent new choices from being made. **Inertia must be its own layer of calculation,
   a question of whether the choices will be followed or whether the inertia will be followed**
   thus the new choices have been made, we know *where* we're going next,
   but we aren't actually using them yet until we hit the next main program in full, N segments later.

   @param choice        to craft arrangements for
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws NexusException on failure
   */
  protected void craftArrangements(SegmentChoice choice, boolean defaultAtonal) throws NexusException {
    // this is used to invert voicings into the tightest possible range
    // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
    NoteRange range = new NoteRange();

    var programConfig = fabricator.getProgramConfig(fabricator.getProgram(choice)
      .orElseThrow(() -> new NexusException("Can't get program config")));

    if (fabricator.getSegmentChords().isEmpty())
      craftArrangementForVoiceSection(choice, 0, fabricator.getSegment().getTotal(), range, defaultAtonal);

    else if (programConfig.doPatternRestartOnChord())
      craftArrangementForVoiceSectionRestartingEachChord(choice, range, defaultAtonal);

    else
      craftArrangementForVoiceSection(choice, 0, fabricator.getSegment().getTotal(), range, defaultAtonal);
  }

  /**
   Precompute all deltas for a given program. This is where deltaIns and deltaOuts values come from.
   <p>
   Precompute deltas dynamically based on whatever is extending the arranger--
   Don't have anything in this class that's proprietary to rhythm or detail-- abstract that out into provider interfaces
   <p>
   Segments have intensity arcs; automate mixer layers in and out of each main program
   https://www.pivotaltracker.com/story/show/178240332
   <p>
   Shift deltas so 2x more time is spent on construction than deconstruction
   https://www.pivotaltracker.com/story/show/179138295
   <p>
   Vary the high plateau between delta in and out across layers
   https://www.pivotaltracker.com/story/show/179126967

   @throws NexusException on failure
   */
  protected void precomputeDeltas(Predicate<SegmentChoice> choiceFilter, ChoiceIndexProvider choiceIndexProvider, Collection<String> indexes, double plateauRatio) throws NexusException {
    this.choiceIndexProvider = choiceIndexProvider;
    deltaIns.clear();
    deltaOuts.clear();
    var bTotal = fabricator.getTemplateConfig().getMainProgramLengthMaxDelta(); // total arc length
    double bPlateau = bTotal * plateauRatio; // plateau section in middle with no transitions
    double bFadesTotal = bTotal - bPlateau;
    double bFadeShiftRatio = DELTA_SHIFT_FRONTEND_RATIO + TremendouslyRandom.zeroToLimit(1 - DELTA_SHIFT_FRONTEND_RATIO);
    double bFadeIn = bFadeShiftRatio * bFadesTotal;
    double bFadeOut = (1 - bFadeShiftRatio) * bFadesTotal;
    double bFadeInLayer = bFadeIn / indexes.size(); // space between transitions in
    double bFadeOutLayer = bFadeOut / indexes.size(); // space between transitions out
    double bPreFadeout = bTotal - bFadeOut;
    var order = new ArrayList<>(indexes);

    // everyone gets the same order at first-- wall-to-wall random deltas
    // random order in
    Collections.shuffle(order);
    for (int i = 0; i < order.size(); i++)
      deltaIns.put(order.get(i), (int) Chance.normallyAround((i + 0.5) * bFadeInLayer, bFadeInLayer * 0.3));
    // different random order out
    Collections.shuffle(order);
    for (int i = 0; i < order.size(); i++)
      deltaOuts.put(order.get(i), (int) Chance.normallyAround(bPreFadeout + (i + 0.5) * bFadeOutLayer, bFadeOutLayer * 0.3));
    // then we overwrite the wall-to-wall random values with more specific values depending on the situation
    switch (fabricator.getType()) {
      case PENDING -> {
        // No Op
      }

      case INITIAL -> {
        // randomly override one incoming (deltaIn unlimited) and one outgoing (deltaOut unlimited)
        deltaIns.put(randomFrom(order), DELTA_UNLIMITED);
        deltaOuts.put(randomFrom(order), DELTA_UNLIMITED);
      }

      case CONTINUE -> {
        for (String index : indexes)
          fabricator.retrospective().getChoices().stream()
            .filter(choiceFilter)
            .filter(choice -> {
              try {
                return Objects.equals(index, choiceIndexProvider.get(choice));
              } catch (NexusException e) {
                return false;
              }
            })
            .findAny()
            .ifPresent(choice -> {
              try {
                deltaIns.put(choiceIndexProvider.get(choice), choice.getDeltaIn());
                deltaOuts.put(choiceIndexProvider.get(choice), choice.getDeltaOut());
              } catch (NexusException e) {
                LOG.warn("Failed to carry over outgoing delta because {}", e.getMessage());
              }
            });
      }

      case NEXTMAIN, NEXTMACRO -> {
        // randomly override one outgoing (deltaOut unlimited)
        deltaOuts.put(randomFrom(order), DELTA_UNLIMITED);

        // select one incoming (deltaIn unlimited) based on whichever was the outgoing (deltaOut unlimited) in the segments of the previous main program
        var priorOutgoing = fabricator.retrospective().getChoices().stream()
          .filter(choiceFilter)
          .filter(this::isUnlimitedOut)
          .filter(choice -> {
            try {
              return indexes.contains(choiceIndexProvider.get(choice));
            } catch (NexusException ignored) {
              return false;
            }
          })
          .findAny();
        deltaIns.put(
          priorOutgoing.isPresent()
            ? choiceIndexProvider.get(priorOutgoing.get())
            : randomFrom(order),
          DELTA_UNLIMITED);
      }
    }
  }

  /**
   Get a random string from the collection

   @param from which to get random string
   @return random string from collection
   */
  private String randomFrom(Collection<String> from) {
    return (String) from.toArray()[TremendouslyRandom.zeroToLimit(from.size())];
  }

  /**
   Iterate through all the chords of a sequence and arrange events per each chord
   <p>
   [#176468993] Detail programs can be made to repeat every chord change

   @param choice        from which to craft events
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws NexusException on failure
   */
  private void craftArrangementForVoiceSectionRestartingEachChord(
    SegmentChoice choice,
    NoteRange range,
    boolean defaultAtonal
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
      sections[i].toPos = i < chords.length - 1
        ? chords[i + 1].getPosition()
        : fabricator.getSegment().getTotal();
    }
    for (var section : sections)
      craftArrangementForVoiceSection(choice, section.fromPos, section.toPos, range, defaultAtonal);
  }

  /**
   Craft events for a section of one detail voice

   @param choice        from which to craft events
   @param fromPos       position (in beats)
   @param maxPos        position (in beats)
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws NexusException on failure
   */
  private void craftArrangementForVoiceSection(
    SegmentChoice choice,
    double fromPos,
    double maxPos,
    NoteRange range,
    boolean defaultAtonal
  ) throws NexusException {
    // choose intro pattern (if available)
    Optional<ProgramSequencePattern> introPattern =
      fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice, ProgramSequencePatternType.INTRO);

    // choose outro pattern (if available)
    Optional<ProgramSequencePattern> outroPattern =
      fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice, ProgramSequencePatternType.OUTRO);

    // compute in and out points, and length # beats for which loop patterns will be required
    double loopOutPos = maxPos - (outroPattern.map(ProgramSequencePattern::getTotal).orElse(0));

    // begin at the beginning and fabricate events for the segment of beginning to end
    double curPos = fromPos;

    // if intro pattern, fabricate those voice event first
    if (introPattern.isPresent())
      curPos += craftPatternEvents(choice, introPattern.get(), curPos, loopOutPos, range, defaultAtonal);

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < loopOutPos) {
      Optional<ProgramSequencePattern> loopPattern =
        fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice, ProgramSequencePatternType.LOOP);
      if (loopPattern.isPresent())
        curPos += craftPatternEvents(choice, loopPattern.get(), curPos, loopOutPos, range, defaultAtonal);
      else
        curPos = loopOutPos;
    }

    // if outro pattern, fabricate those voice event last
    // [#161466708] compute how much to go for it in the outro
    if (outroPattern.isPresent())
      craftPatternEvents(choice, outroPattern.get(), curPos, maxPos, range, defaultAtonal);
  }

  /**
   Craft the voice events of a single pattern.
   [#161601279] Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

   @param pattern             to source events
   @param fromSegmentPosition to write events to segment
   @param toSegmentPosition   to write events to segment
   @param range               used to keep voicing in the tightest range possible
   @param defaultAtonal       whether to default to a single atonal note, if no voicings are available
   @return deltaPos of start, after crafting this batch of pattern events
   */
  private double craftPatternEvents(
    SegmentChoice choice,
    ProgramSequencePattern pattern,
    double fromSegmentPosition,
    double toSegmentPosition,
    NoteRange range,
    boolean defaultAtonal
  ) throws NexusException {
    if (Objects.isNull(pattern)) throw new NexusException("Cannot craft create null pattern");
    double totalBeats = toSegmentPosition - fromSegmentPosition;
    Collection<ProgramSequencePatternEvent> events = fabricator.sourceMaterial().getEvents(pattern);

    SegmentChoiceArrangement arrangement = fabricator.add(new SegmentChoiceArrangement()
      .id(UUID.randomUUID())
      .segmentId(choice.getSegmentId())
      .segmentChoiceId(choice.getId())
      .programSequencePatternId(pattern.getId()));

    var instrument = fabricator.sourceMaterial().getInstrument(choice.getInstrumentId())
      .orElseThrow(() -> new NexusException("Failed to retrieve instrument"));
    for (ProgramSequencePatternEvent event : events)
      pickNotesAndInstrumentAudioForEvent(instrument, choice, arrangement, event, fromSegmentPosition, toSegmentPosition, range, defaultAtonal);
    return Math.min(totalBeats, pattern.getTotal());
  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param choice              to pick notes for

   @param event               to pick audio for
   @param fromSegmentPosition to pick notes for
   @param toSegmentPosition   to pick notes for
   @param range               used to keep voicing in the tightest range possible
   @param defaultAtonal       whether to default to a single atonal note, if no voicings are available
   */
  private void pickNotesAndInstrumentAudioForEvent(
    Instrument instrument,
    SegmentChoice choice,
    SegmentChoiceArrangement arrangement,
    ProgramSequencePatternEvent event,
    Double fromSegmentPosition,
    Double toSegmentPosition,
    NoteRange range,
    boolean defaultAtonal
  ) throws NexusException {
    // Morph & Point attributes are expressed in beats
    double segmentPosition = fromSegmentPosition + event.getPosition();
    double duration = Math.min(event.getDuration(), toSegmentPosition - segmentPosition);
    var chord = fabricator.getChordAt(segmentPosition);
    Optional<SegmentChordVoicing> voicing = chord.isPresent()
      ? fabricator.getVoicing(chord.get(), instrument.getType())
      : Optional.empty();

    var volRatio = computeVolumeRatioForPickedNote(choice, segmentPosition);
    if (0 >= volRatio) return;

    // The final note is voiced from the chord voicing (if found) or else the default is used
    Set<String> notes = voicing.isPresent()
      ? pickNotesForEvent(instrument.getType(), choice, event, chord.get(), voicing.get(), range)
      : (defaultAtonal ? Set.of(Note.ATONAL) : Set.of());

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = fabricator.getSecondsAtPosition(segmentPosition);
    double lengthSeconds = fabricator.getSecondsAtPosition(segmentPosition + duration) - startSeconds;

    // pick an audio for each note
    for (var note : notes)
      pickInstrumentAudio(note, instrument, event, arrangement, startSeconds, lengthSeconds,
        voicing.map(SegmentChordVoicing::getId).orElse(null), volRatio);
  }

  /**
   Compute the volume ratio of a picked note

   @param choice          for which to compute volume ratio
   @param segmentPosition at which to compute
   @return volume ratio
   */
  private double computeVolumeRatioForPickedNote(SegmentChoice choice, double segmentPosition) {
    return switch (choice.getInstrumentType()) {
      case DRUM, STAB -> computeVolumeRatioForPickedNote(choice, segmentPosition, false, false, true);
      case BASS -> computeVolumeRatioForPickedNote(choice, segmentPosition, false, false, false);
      case PERCUSSIONLOOP -> computeVolumeRatioForPickedNote(choice, segmentPosition, true, false, false);
      case PAD, STICKY, STRIPE -> computeVolumeRatioForPickedNote(choice, segmentPosition, false, true, true);
    };
  }

  /**
   Compute the volume ratio of a picked note
   <p>
   [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program

   @param choice          for which to compute volume ratio
   @param segmentPosition at which to compute
   @param topOfSegment    if should appear at the top of the segment where its delta in appears
   @param fadeIn          if deltaIn should fade in, else start right on cue
   @param fadeOut         if deltaOut should fade out, else stop right on cue
   @return volume ratio
   */
  private double computeVolumeRatioForPickedNote(SegmentChoice choice, double segmentPosition, boolean topOfSegment, boolean fadeIn, boolean fadeOut) {
    if (!fabricator.getTemplateConfig().isChoiceDeltaEnabled()) return 1.0;

    // if deltaIn is before the beginning of this segment and deltaOut is after, include it
    if (choice.getDeltaIn() < fabricator.getSegment().getDelta()
      && choice.getDeltaOut() > fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal())
      return 1;

    // if deltaIn is past the end of this segment, exclude
    if (!isUnlimitedIn(choice)
      && choice.getDeltaIn() > fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal())
      return 0;

    // if deltaOut is before the beginning of this segment, exclude
    if (!isUnlimitedOut(choice)
      && choice.getDeltaOut() <= fabricator.getSegment().getDelta())
      return 0;

    // If position is between the beginning of the segment at the deltaIn, either fade in or silence
    if (!isUnlimitedIn(choice)
      && choice.getDeltaIn() > fabricator.getSegment().getDelta()
      && fabricator.getSegment().getDelta() + segmentPosition < choice.getDeltaIn())
      if (fadeIn)
        return segmentPosition / (choice.getDeltaIn() - fabricator.getSegment().getDelta());
      else if (topOfSegment)
        return 1;
      else
        return 0;

    // If position is between the deltaOut and the end of the segment, either fade out or leave it alone
    if (!isUnlimitedOut(choice)
      && choice.getDeltaOut() <= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal()
      && fabricator.getSegment().getDelta() + segmentPosition > choice.getDeltaOut())
      if (fadeOut)
        return 1.0 - (segmentPosition - (choice.getDeltaOut() - fabricator.getSegment().getDelta())) / (fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal() - choice.getDeltaOut());
      else
        return 1.0;

    return 1.0;
  }

  /**
   Whether a given choice has deltaIn unlimited

   @param choice to test
   @return true if deltaIn is unlimited
   */
  protected boolean isUnlimitedIn(SegmentChoice choice) {
    return DELTA_UNLIMITED == getDeltaIn(choice);
  }

  /**
   Whether a given choice has deltaOut unlimited

   @param choice to test
   @return true if deltaOut is unlimited
   */
  protected boolean isUnlimitedOut(SegmentChoice choice) {
    return DELTA_UNLIMITED == getDeltaOut(choice);
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
    InstrumentType instrumentType,
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
    var voicingNotes = fabricator.getNotes(voicing).stream()
      .flatMap(Note::ofValid)
      .collect(Collectors.toList());

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
    @Nullable UUID segmentChordVoicingId,
    double volRatio
  ) throws NexusException {
    var audio =
      fabricator.getInstrumentConfig(instrument).isMultiphonic()
        ? selectMultiphonicInstrumentAudio(instrument, event, note)
        : selectInstrumentAudio(instrument, event);

    // [#176373977] Should gracefully skip audio in unfulfilled by instrument
    if (audio.isEmpty()) return;

    // of pick
    var pick = new SegmentChoiceArrangementPick()
      .id(UUID.randomUUID())
      .segmentId(segmentChoiceArrangement.getSegmentId())
      .segmentChoiceArrangementId(segmentChoiceArrangement.getId())
      .instrumentAudioId(audio.get().getId())
      .programSequencePatternEventId(event.getId())
      .name(fabricator.getTrackName(event))
      .start(startSeconds)
      .length(lengthSeconds)
      .amplitude(event.getVelocity() * volRatio)
      .note(fabricator.getInstrumentConfig(instrument).isTonal() ? note : Note.ATONAL);
    if (Objects.nonNull(segmentChordVoicingId))
      pick.setSegmentChordVoicingId(segmentChordVoicingId);
    fabricator.add(pick);
  }

  /**
   Select audio from a multiphonic instrument
   <p>
   [#176649593] Sampler obeys isMultiphonic from Instrument config

   @param instrument of which to score available audios, and make a selection
   @param event      for caching reference
   @param note       to match selection
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
   @param event      to match selection
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
      if (instrument.getType() == InstrumentType.DRUM)
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
    var a = Note.of(note);
    var audio = instrumentAudios
      .stream()
      .filter(candidate -> {
        if (Objects.isNull(candidate) || Strings.isNullOrEmpty(candidate.getNote())) return false;
        var b = Note.of(candidate.getNote());
        return a.isAtonal() || b.isAtonal() || a.sameAs(b);
      })
      .findAny();

    if (audio.isEmpty()) {
      reportMissingInstrumentAudio(ImmutableMap.of(
        "instrumentId", instrument.getId().toString(),
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

  /**
   Class to get a comparable string index based on any given choice, e.g. it's voice name or instrument type
   */
  public interface ChoiceIndexProvider {
    String get(SegmentChoice choice) throws NexusException;
  }

  /**
   Default choice index provider
   */
  public static class DefaultChoiceIndexProvider implements ChoiceIndexProvider {
    @Override
    public String get(SegmentChoice choice) throws NexusException {
      return choice.getId().toString();
    }
  }
}
