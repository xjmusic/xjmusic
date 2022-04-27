// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentType;
import io.xj.hub.client.HubClientException;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.Entities;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Bar;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.util.CSV;
import io.xj.lib.util.MarbleBag;
import io.xj.lib.util.Text;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.FabricationWrapperImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import io.xj.nexus.fabricator.NameIsometry;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.persistence.Segments.DELTA_UNLIMITED;

/**
 Arrangement of Segment Events is a common foundation for all craft
 */
public class CraftImpl extends FabricationWrapperImpl {
  private final Map<String, Integer> deltaIns = Maps.newHashMap();
  private final Map<String, Integer> deltaOuts = Maps.newHashMap();
  private final List<InstrumentType> finalizeAudioLengthsForInstrumentTypes;
  private ChoiceIndexProvider choiceIndexProvider = new DefaultChoiceIndexProvider();

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  @Inject
  public CraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);

    finalizeAudioLengthsForInstrumentTypes = fabricator.getTemplateConfig().getInstrumentTypesForAudioLengthFinalization();
  }

  /**
   Whether a given choice has deltaIn unlimited

   @param choice to test
   @return true if deltaIn is unlimited
   */
  protected static boolean isUnlimitedIn(SegmentChoice choice) {
    return Objects.nonNull(choice.getDeltaIn()) && DELTA_UNLIMITED == choice.getDeltaIn();
  }

  /**
   Whether a given choice has deltaOut unlimited

   @param choice to test
   @return true if deltaOut is unlimited
   */
  protected static boolean isUnlimitedOut(SegmentChoice choice) {
    return Objects.nonNull(choice.getDeltaOut()) && DELTA_UNLIMITED == choice.getDeltaOut();
  }

  /**
   Whether a position is in the given bounds

   @param floor   of boundary
   @param ceiling of boundary
   @param value   to test for within bounds
   @return true if value is within bounds (inclusive)
   */
  static boolean inBounds(Integer floor, Integer ceiling, double value) {
    if (DELTA_UNLIMITED == floor && DELTA_UNLIMITED == ceiling) return true;
    if (DELTA_UNLIMITED == floor && value <= ceiling) return true;
    if (DELTA_UNLIMITED == ceiling && value >= floor) return true;
    return value >= floor && value <= ceiling;
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
  protected void craftNoteEvents(ProgramSequence sequence, Collection<ProgramVoice> voices, InstrumentProvider instrumentProvider, boolean defaultAtonal) throws NexusException {
    // Craft each voice into choice
    try {
      for (ProgramVoice voice : voices) {
        var choice = new SegmentChoice();

        choice.setId(UUID.randomUUID());
        choice.setProgramType(fabricator.sourceMaterial().getProgram(voice.getProgramId())
          .orElseThrow(() -> new NexusException("Can't get program for voice")).getType()
          .toString());
        choice.setInstrumentType(voice.getType().toString());
        choice.setProgramId(voice.getProgramId());
        choice.setProgramSequenceId(sequence.getId());
        choice.setProgramVoiceId(voice.getId());
        choice.setSegmentId(fabricator.getSegment().getId());

        // Whether there is a prior choice for this voice
        Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(voice);

        if (priorChoice.isPresent()) {
          choice.setDeltaIn(priorChoice.get().getDeltaIn());
          choice.setDeltaOut(priorChoice.get().getDeltaOut());
          choice.setInstrumentId(priorChoice.get().getInstrumentId());
          this.craftNoteEventArrangements(fabricator.put(choice), defaultAtonal);
          continue;
        }

        var instrument = instrumentProvider.get(voice);
        if (instrument.isEmpty()) {
          reportMissing(Instrument.class, String.format("%s-type instrument", voice.getType()));
          continue;
        }

        // make new choices
        choice.setDeltaIn(computeDeltaIn(choice));
        choice.setDeltaOut(computeDeltaOut(choice));
        choice.setInstrumentId(instrument.get().getId());
        this.craftNoteEventArrangements(fabricator.put(choice), defaultAtonal);
      }

    } catch (HubClientException e) {
      throw new NexusException(e);
    }
  }

  /**
   ChordPart instrument mode
   https://www.pivotaltracker.com/story/show/181631275

   @param instrument for which to craft choices
   @throws NexusException on failure
   */
  protected void craftChordParts(Instrument instrument) throws NexusException {
    // Craft each voice into choice
    var choice = new SegmentChoice();

    choice.setId(UUID.randomUUID());
    choice.setInstrumentType(instrument.getType().toString());
    choice.setInstrumentId(instrument.getId());
    choice.setSegmentId(fabricator.getSegment().getId());

    // Whether there is a prior choice for this voice
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(instrument.getType());

    if (priorChoice.isPresent()) {
      choice.setDeltaIn(priorChoice.get().getDeltaIn());
      choice.setDeltaOut(priorChoice.get().getDeltaOut());
      choice.setInstrumentId(priorChoice.get().getInstrumentId());
      this.craftChordParts(instrument, fabricator.put(choice));
      return;
    }

    // make new choices
    choice.setDeltaIn(computeDeltaIn(choice));
    choice.setDeltaOut(computeDeltaOut(choice));
    choice.setInstrumentId(instrument.getId());
    this.craftChordParts(instrument, fabricator.put(choice));
  }

  /**
   ChordPart instrument mode
   https://www.pivotaltracker.com/story/show/181631275

   @param instrument chosen
   @param choice     for which to craft chord parts
   @throws NexusException on failure
   */
  protected void craftChordParts(Instrument instrument, SegmentChoice choice) throws NexusException {
    if (fabricator.getSegmentChords().isEmpty())
      return;

    // Arrangement
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(choice.getSegmentId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Pick for each section
    for (var section : computeSections()) {
      var audio =
        selectChordPartInstrumentAudio(instrument, Chord.of(section.chord.getName()));

      // https://www.pivotaltracker.com/story/show/176373977 Should gracefully skip audio in unfulfilled by instrument
      if (audio.isEmpty()) return;

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.getSecondsAtPosition(section.fromPos);
      @Nullable Double lengthSeconds = fabricator.isOneShot(instrument)
        ? null
        : fabricator.getSecondsAtPosition(section.toPos) - startSeconds;

      // Volume ratio
      var volRatio = computeVolumeRatioForPickedNote(choice, section.fromPos);
      if (0 >= volRatio) continue;

      // Pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(choice.getSegmentId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setInstrumentAudioId(audio.get().getId());
      pick.setTones(section.chord.getName());
      pick.setEvent(Text.toEvent(instrument.getType().toString()));
      pick.setLength(lengthSeconds);
      pick.setAmplitude(volRatio);
      fabricator.put(pick);
    }

    // Final pass to set the actual length of one-shot audio picks
    finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choice);
  }

  /**
   Get the delta in for the given voice

   @param choice for which to get delta in
   @return delta in for given voice
   */
  protected int computeDeltaIn(SegmentChoice choice) {
    return deltaIns.getOrDefault(choiceIndexProvider.get(choice), DELTA_UNLIMITED);
  }

  /**
   Get the delta out for the given voice

   @param choice for which to get delta out
   @return delta out for given voice
   */
  protected int computeDeltaOut(SegmentChoice choice) {
    return deltaOuts.getOrDefault(choiceIndexProvider.get(choice), DELTA_UNLIMITED);
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
   <p>
   Ends with a final pass to set the actual length of one-shot audio picks
   One-shot instruments cut off when other notes played with same instrument, or at end of segment https://www.pivotaltracker.com/story/show/180245315

   @param choice        to craft arrangements for
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws NexusException on failure
   */
  protected void craftNoteEventArrangements(SegmentChoice choice, boolean defaultAtonal) throws NexusException, HubClientException {
    // this is used to invert voicings into the tightest possible range
    // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
    NoteRange range = NoteRange.empty();

    var programConfig = fabricator.getProgramConfig(fabricator.getProgram(choice)
      .orElseThrow(() -> new NexusException("Can't get program config")));

    if (fabricator.getSegmentChords().isEmpty())
      craftNoteEventSection(choice, 0, fabricator.getSegment().getTotal(), range, defaultAtonal);

    else if (programConfig.doPatternRestartOnChord())
      craftNoteEventSectionRestartingEachChord(choice, range, defaultAtonal);

    else
      craftNoteEventSection(choice, 0, fabricator.getSegment().getTotal(), range, defaultAtonal);

    // Final pass to set the actual length of one-shot audio picks
    finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choice);
  }

  /**
   Precompute all deltas for a given program. This is where deltaIns and deltaOuts values come from.
   <p>
   Precompute deltas dynamically based on whatever is extending the arranger--
   Don't have anything in this class that's proprietary to beat or detail-- abstract that out into provider interfaces
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
  protected void precomputeDeltas(Predicate<SegmentChoice> choiceFilter, ChoiceIndexProvider choiceIndexProvider, Collection<String> layers, Collection<String> layerPrioritizationSearches, int numLayersIncoming) throws NexusException {
    this.choiceIndexProvider = choiceIndexProvider;
    deltaIns.clear();
    deltaOuts.clear();

    // Ensure that we can bypass delta arcs using the template config
    if (!fabricator.getTemplateConfig().isDeltaArcEnabled()) {
      layers.forEach(layer -> {
        deltaIns.put(layer, DELTA_UNLIMITED);
        deltaOuts.put(layer, DELTA_UNLIMITED);
      });
      return;
    }

    // then we overwrite the wall-to-wall random values with more specific values depending on the situation
    switch (fabricator.getType()) {
      case PENDING -> {
        // No Op
      }

      case INITIAL, NEXTMAIN, NEXTMACRO -> {
        // randomly override N incoming (deltaIn unlimited) and N outgoing (deltaOut unlimited)
        // shuffle the layers into a random order, then step through them, assigning delta ins and then outs
        // random order in
        var barBeats = fabricator.getCurrentMainProgramConfig().getBarBeats();
        var deltaUnits = Bar.of(barBeats).computeSubsectionBeats(fabricator.getSegment().getTotal());

        // Delta arcs can prioritize the presence of a layer by name, e.g. containing "kick" https://www.pivotaltracker.com/story/show/180242564
        // separate layers into primary and secondary, shuffle them separately, then concatenate
        List<String> priLayers = Lists.newArrayList();
        List<String> secLayers = Lists.newArrayList();
        layers.forEach(layer -> {
          var layerName = layer.toLowerCase(Locale.ROOT);
          if (layerPrioritizationSearches.stream().anyMatch(m -> layerName.contains(m.toLowerCase(Locale.ROOT))))
            priLayers.add(layer);
          else
            secLayers.add(layer);
        });
        Collections.shuffle(priLayers);
        if (!priLayers.isEmpty())
          fabricator.addInfoMessage(String.format("Prioritized %s", CSV.join(priLayers)));
        Collections.shuffle(secLayers);
        var orderedLayers = Stream.concat(priLayers.stream(), secLayers.stream()).toList();
        var delta = Values.roundToNearest(deltaUnits, TremendouslyRandom.zeroToLimit(deltaUnits * 4) - deltaUnits * 2 * numLayersIncoming);
        for (String orderedLayer : orderedLayers) {
          deltaIns.put(orderedLayer, delta > 0 ? delta : DELTA_UNLIMITED);
          deltaOuts.put(orderedLayer, DELTA_UNLIMITED); // all layers get delta out unlimited
          delta += Values.roundToNearest(deltaUnits, TremendouslyRandom.zeroToLimit(deltaUnits * 5));
        }
      }

      case CONTINUE -> {
        for (String index : layers)
          fabricator.retrospective().getChoices().stream()
            .filter(choiceFilter)
            .filter(choice -> Objects.equals(index, choiceIndexProvider.get(choice)))
            .findAny()
            .ifPresent(choice -> deltaIns.put(choiceIndexProvider.get(choice), choice.getDeltaIn()));
      }
    }
  }

  /**
   Iterate through all the chords of a sequence and arrange events per each chord
   <p>
   https://www.pivotaltracker.com/story/show/176468993 Detail programs can be made to repeat every chord change

   @param choice        from which to craft events
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws NexusException on failure
   */
  private void craftNoteEventSectionRestartingEachChord(
    SegmentChoice choice,
    NoteRange range,
    boolean defaultAtonal
  ) throws NexusException {
    for (var section : computeSections())
      craftNoteEventSection(choice, section.fromPos, section.toPos, range, defaultAtonal);
  }

  /**
   Compute the segment chord sections

   @return sections in order of position ascending
   */
  private List<Section> computeSections() {
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
    return Arrays.stream(sections).toList();
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
  private void craftNoteEventSection(
    SegmentChoice choice,
    double fromPos,
    double maxPos,
    NoteRange range,
    boolean defaultAtonal
  ) throws NexusException {

    // begin at the beginning and fabricate events for the segment of beginning to end
    double curPos = fromPos;

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < maxPos) {
      Optional<ProgramSequencePattern> loopPattern =
        fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice);
      if (loopPattern.isPresent())
        curPos += craftPatternEvents(choice, loopPattern.get(), curPos, maxPos, range, defaultAtonal);
      else
        curPos = maxPos;
    }
  }

  /**
   Craft the voice events of a single pattern.
   https://www.pivotaltracker.com/story/show/161601279 Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

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
    List<ProgramSequencePatternEvent> events = fabricator.sourceMaterial().getEvents(pattern);

    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(choice.getSegmentId());
    arrangement.segmentChoiceId(choice.getId());
    arrangement.setProgramSequencePatternId(pattern.getId());
    fabricator.put(arrangement);

    var instrument = fabricator.sourceMaterial().getInstrument(choice.getInstrumentId())
      .orElseThrow(() -> new NexusException("Failed to retrieve instrument"));
    for (ProgramSequencePatternEvent event : events)
      pickNotesAndInstrumentAudioForEvent(instrument, choice, arrangement, fromSegmentPosition, toSegmentPosition, event, range, defaultAtonal);
    return Math.min(totalBeats, pattern.getTotal());
  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param choice              to pick notes for
   @param fromSegmentPosition to pick notes for
   @param toSegmentPosition   to pick notes for
   @param event               to pick audio for
   @param range               used to keep voicing in the tightest range possible
   @param defaultAtonal       whether to default to a single atonal note, if no voicings are available
   */
  private void pickNotesAndInstrumentAudioForEvent(
    Instrument instrument,
    SegmentChoice choice,
    SegmentChoiceArrangement arrangement,
    Double fromSegmentPosition,
    Double toSegmentPosition,
    ProgramSequencePatternEvent event,
    NoteRange range,
    boolean defaultAtonal
  ) throws NexusException {
    // Morph & Point attributes are expressed in beats
    double segmentPosition = fromSegmentPosition + event.getPosition();

    // Should never place segment events outside of segment time range https://www.pivotaltracker.com/story/show/180245354
    if (segmentPosition < 0 || segmentPosition >= fabricator.getSegment().getTotal()) return;

    double duration = Math.min(event.getDuration(), toSegmentPosition - segmentPosition);
    var chord = fabricator.getChordAt(segmentPosition);
    Optional<SegmentChordVoicing> voicing = chord.isPresent()
      ? fabricator.getVoicing(chord.get(), instrument.getType())
      : Optional.empty();

    var volRatio = computeVolumeRatioForPickedNote(choice, segmentPosition);
    if (0 >= volRatio) return;

    // The final note is voiced from the chord voicing (if found) or else the default is used
    Set<String> notes = voicing.isPresent()
      ? pickNotesForEvent(instrument.getType(), choice, event, segmentPosition, chord.get(), voicing.get(), range)
      : (defaultAtonal ? Set.of(Note.ATONAL) : Set.of());

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = fabricator.getSecondsAtPosition(segmentPosition);
    @Nullable Double lengthSeconds = fabricator.isOneShot(instrument, fabricator.getTrackName(event))
      ? null
      : fabricator.getSecondsAtPosition(segmentPosition + duration) - startSeconds;

    // pick an audio for each note
    for (var note : notes)
      pickInstrumentAudio(note, instrument, event, arrangement, startSeconds, lengthSeconds,
        voicing.map(SegmentChordVoicing::getId).orElse(null), volRatio);
  }

  /**
   Ends with a final pass to set the actual length of one-shot audio picks
   One-shot instruments cut off when other notes played with same instrument, or at end of segment https://www.pivotaltracker.com/story/show/180245315

   @param choice for which to finalize length of one-shot audio picks
   */
  private void finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(SegmentChoice choice) throws NexusException {
    var instrument = fabricator.sourceMaterial().getInstrument(choice.getInstrumentId())
      .orElseThrow(() -> new NexusException("Failed to get instrument from source material for segment choice!"));

    // skip instruments that are not one-shot
    if (!fabricator.isOneShot(instrument))
      return;

    // skip instruments that are do not have one-shot cutoff enabled https://www.pivotaltracker.com/story/show/181211927
    if (!fabricator.isOneShotCutoffEnabled(instrument))
      return;

    // skip instruments that are not on the list
    if (!finalizeAudioLengthsForInstrumentTypes.contains(instrument.getType()))
      return;

    // get all the picks, ordered chronologically, and skip the rest of this process if there are none
    List<SegmentChoiceArrangementPick> picks = fabricator.getPicks(choice);
    if (picks.isEmpty()) return;

    // build an ordered unique list of the moments in time when the one-shot will be cut off
    List<Double> cutoffs = picks.stream()
      .map(SegmentChoiceArrangementPick::getStart)
      .collect(Collectors.toSet()).stream()
      .sorted()
      .toList();

    // iterate and set lengths of all picks in series
    for (SegmentChoiceArrangementPick pick : picks) {

      // Skip picks that already have their end length set
      if (Objects.nonNull(pick.getLength())) continue;

      var nextCutoff = cutoffs.stream()
        .filter(c -> c > pick.getStart())
        .findFirst();

      if (nextCutoff.isPresent()) {
        pick.setLength(nextCutoff.get() - pick.getStart());
        fabricator.put(pick);
        continue;
      }

      if (pick.getStart() < fabricator.getTotalSeconds()) {
        pick.setLength(fabricator.getTotalSeconds() - pick.getStart());
        fabricator.put(pick);
        continue;
      }

      fabricator.delete(pick);
    }
  }

  /**
   Compute the volume ratio of a picked note

   @param choice          for which to compute volume ratio
   @param segmentPosition at which to compute
   @return volume ratio
   */
  private double computeVolumeRatioForPickedNote(SegmentChoice choice, double segmentPosition) {
    if (!fabricator.getTemplateConfig().isDeltaArcEnabled()) return 1.0;
    return inBounds(choice.getDeltaIn(), choice.getDeltaOut(), fabricator.getSegment().getDelta() + segmentPosition) ? 1.0 : 0.0;
  }

  /**
   Whether the current segment contains the delta in for the given choice

   @param choice to test whether the current segment contains this choice delta in
   @return true if the current segment contains the given choice's delta in
   */
  public boolean isIntroSegment(SegmentChoice choice) {
    return !isUnlimitedIn(choice)
      && choice.getDeltaIn() >= fabricator.getSegment().getDelta()
      && choice.getDeltaIn() < fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal();
  }

  /**
   Whether the current segment contains the delta out for the given choice

   @param choice to test whether the current segment contains this choice delta out
   @return true if the current segment contains the given choice's delta out
   */
  public boolean isOutroSegment(SegmentChoice choice) {
    return !isUnlimitedOut(choice)
      && choice.getDeltaOut() <= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal()
      && choice.getDeltaOut() > fabricator.getSegment().getDelta();
  }

  /**
   Whether the given choice is silent during the entire segment

   @param choice to test for silence
   @return true if choice is silent the entire segment
   */
  public boolean isSilentEntireSegment(SegmentChoice choice) {
    return
      (choice.getDeltaOut() < fabricator.getSegment().getDelta())
        || (choice.getDeltaIn() >= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal());
  }

  /**
   Whether the given choice is fully active during the current segment

   @param choice to test for activation
   @return true if this choice is active the entire time
   */
  public boolean isActiveEntireSegment(SegmentChoice choice) {
    return (choice.getDeltaIn() <= fabricator.getSegment().getDelta())
      && (choice.getDeltaOut() >= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal());
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   https://www.pivotaltracker.com/story/show/176695166 XJ should choose correct instrument note based on detail program note

   @param instrumentType  comprising audios
   @param choice          for reference
   @param event           of program to pick instrument note for
   @param segmentPosition of note
   @param segmentChord    to use for interpreting the voicing
   @param voicing         to choose a note from
   @param range           used to keep voicing in the tightest range possible
   @return note picked from the available voicing
   */
  private Set<String> pickNotesForEvent(
    InstrumentType instrumentType,
    SegmentChoice choice,
    ProgramSequencePatternEvent event,
    Double segmentPosition,
    SegmentChord segmentChord,
    SegmentChordVoicing voicing,
    NoteRange range
  ) throws NexusException {
    var previous = fabricator.getPreferredNotes(event.getId().toString(), segmentChord.getName());
    if (previous.isPresent()) return previous.get();

    // Various computations to prepare for picking
    var chord = Chord.of(segmentChord.getName());
    var sourceKey = fabricator.getKeyForChoice(choice);
    var sourceRange = fabricator.getProgramRange(choice.getProgramId(), instrumentType);
    var targetRange = fabricator.getProgramVoicingNoteRange(instrumentType);
    var targetShiftSemitones = fabricator.getProgramTargetShift(sourceKey, Chord.of(chord.getName()));
    var targetShiftOctaves = fabricator.getProgramRangeShiftOctaves(instrumentType,
      sourceRange.shifted(targetShiftSemitones), // take semitone shift into account before computing octave shift! https://www.pivotaltracker.com/story/show/181975107
      targetRange);
    var voicingNotes = fabricator.getNotes(voicing).stream()
      .flatMap(Note::ofValid)
      .collect(Collectors.toList());
    var rootNote = fabricator.getRootNoteMidRange(voicing.getNotes(), chord);

    // Event notes are either computed from sticky bun or interpreted from program, potentially at random
    List<Note> eventNotes = CSV.split(event.getTones())
      .stream()
      .map(n -> Note.of(n).shift(targetShiftSemitones + 12 * targetShiftOctaves))
      .collect(Collectors.toList());

    // Sticky buns v2 https://www.pivotaltracker.com/story/show/179153822 uses this alternate path for note selection of random ("X") note events
    var bun = fabricator.getStickyBun(event.getProgramSequencePatternId());
    if (bun.isPresent() && bun.get().isTonal(event.getId()) && rootNote.isPresent())
      eventNotes = bun.get().replaceAtonal(event.getId(), rootNote.get(), eventNotes);

    var eventDeltaSemitones = sourceRange.shifted(targetShiftSemitones).getDeltaSemitones(NoteRange.ofNotes(eventNotes));
    var eventRange = NoteRange.ofNotes(eventNotes).shifted(eventDeltaSemitones);
    if (range.isEmpty() && !eventRange.isEmpty()) range.expand(eventRange);

    var notePicker = new NotePicker(range.shifted(eventDeltaSemitones), voicingNotes, eventNotes,
      fabricator.getTemplateConfig().getInstrumentTypesForInversionSeeking().contains(instrumentType));

    notePicker.pick();

    var notes = notePicker.getPickedNotes().stream()
      .map(n -> n.toString(chord.getAdjSymbol())).collect(Collectors.toSet());

    fabricator.putNotesPickedForChord(event, chord.getName(), notes);

    // Sticky buns v2 https://www.pivotaltracker.com/story/show/179153822 persisted for each randomly selected note in the series for any given pattern
    rootNote.ifPresent(note ->
      fabricator.putStickyBun(event.getId(), note, segmentPosition, notes.stream().map(Note::of).toList()));

    return notes;
  }

  /**
   https://www.pivotaltracker.com/story/show/176696738 XJ has a serviceable voicing algorithm
   <p>
   https://www.pivotaltracker.com/story/show/176474113 Artist can edit comma-separated notes into detail program events
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
    @Nullable Double lengthSeconds,
    @Nullable UUID segmentChordVoicingId,
    double volRatio
  ) throws NexusException {
    var audio =
      fabricator.getInstrumentConfig(instrument).isMultiphonic()
        ? selectMultiphonicInstrumentAudio(instrument, event, note)
        : selectNoteEventInstrumentAudio(instrument, event);

    // https://www.pivotaltracker.com/story/show/176373977 Should gracefully skip audio in unfulfilled by instrument
    if (audio.isEmpty()) return;

    // of pick
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(segmentChoiceArrangement.getSegmentId());
    pick.setSegmentChoiceArrangementId(segmentChoiceArrangement.getId());
    pick.setInstrumentAudioId(audio.get().getId());
    pick.setProgramSequencePatternEventId(event.getId());
    pick.setEvent(fabricator.getTrackName(event));
    pick.setStart(startSeconds);
    pick.setLength(lengthSeconds);
    pick.setAmplitude(event.getVelocity() * volRatio);
    pick.setTones(fabricator.getInstrumentConfig(instrument).isTonal() ? note : Note.ATONAL);
    if (Objects.nonNull(segmentChordVoicingId))
      pick.setSegmentChordVoicingId(segmentChordVoicingId);
    fabricator.put(pick);
  }

  /**
   Select audio from a multiphonic instrument
   <p>
   https://www.pivotaltracker.com/story/show/176649593 Sampler obeys isMultiphonic from Instrument config

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
    if (fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), note).isEmpty()) {
      var audio = selectNewMultiphonicInstrumentAudio(instrument, note);
      audio.ifPresent(instrumentAudio -> fabricator.putPreferredAudio(event.getProgramVoiceTrackId().toString(), note, instrumentAudio));
    }

    return fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), note);
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
  private Optional<InstrumentAudio> selectNoteEventInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    if (fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones()).isEmpty())
      fabricator.putPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones(),
        selectNewNoteEventInstrumentAudio(instrument, event)
          .orElseThrow(() -> new NexusException("Unable to select note event instrument audio!")));

    return fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones());
  }

  /**
   ChordPart instrument mode
   https://www.pivotaltracker.com/story/show/181631275
   <p>
   If never encountered, default to new selection and cache that.

   @param instrument of which to score available audios, and make a selection
   @param chord      to match selection
   @return matched new audio
   */
  private Optional<InstrumentAudio> selectChordPartInstrumentAudio(
    Instrument instrument,
    Chord chord
  ) {
    if (fabricator.getPreferredAudio(instrument.getId().toString(), chord.getName()).isEmpty()) {
      var audio = selectNewChordPartInstrumentAudio(instrument, chord);
      audio.ifPresent(instrumentAudio -> fabricator.putPreferredAudio(instrument.getId().toString(), chord.getName(), instrumentAudio));
    }

    return fabricator.getPreferredAudio(instrument.getId().toString(), chord.getName());
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   @return matched new audio
   */
  private Optional<InstrumentAudio> selectNewNoteEventInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    Map<UUID, Integer> score = Maps.newHashMap();

    // add all audio to chooser
    fabricator.sourceMaterial().getAudios(instrument)
      .forEach(a -> score.put(a.getId(), 0));

    // score each audio against the current voice event, with some variability
    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudios(instrument))
      if (instrument.getType() == InstrumentType.Drum)
        score.put(audio.getId(), NameIsometry.similarity(fabricator.getTrackName(event), audio.getEvent()));
      else
        score.put(audio.getId(), Note.of(audio.getTones()).sameAs(Note.of(event.getTones())) ? 100 : 0);

    // final chosen audio event
    var pickId = Values.getKeyOfHighestValue(score);
    return pickId.isPresent() ? fabricator.sourceMaterial().getInstrumentAudio(pickId.get()) : Optional.empty();
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param chord      to match
   @return matched new audio
   */
  private Optional<InstrumentAudio> selectNewChordPartInstrumentAudio(
    Instrument instrument,
    Chord chord
  ) {
    var bag = MarbleBag.empty();

    Chord audioChord;
    for (var a : fabricator.sourceMaterial().getAudios(instrument)) {
      audioChord = Chord.of(a.getTones());
      if (audioChord.equals(chord))
        bag.add(0, a.getId());
      else if (audioChord.hasSamePitchClasses(chord))
        bag.add(1, a.getId());
    }

    if (bag.isEmpty()) return Optional.empty();

    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

  /**
   Select a new random instrument audio based on a pattern event
   <p>
   https://www.pivotaltracker.com/story/show/176649593 Sampler obeys isMultiphonic from Instrument config

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
        if (Objects.isNull(candidate) || Strings.isNullOrEmpty(candidate.getTones())) return false;
        var b = Note.of(candidate.getTones());
        return a.isAtonal() || b.isAtonal() || a.sameAs(b);
      })
      .findAny();

    if (audio.isEmpty()) {
      reportMissing(ImmutableMap.of(
        "instrumentId", instrument.getId().toString(),
        "searchForNote", note,
        "availableNotes", CSV.from(instrumentAudios
          .stream()
          .map(InstrumentAudio::getTones)
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
   Choose a fresh program based on a set of memes

   @param programType to choose
   @param voicingType (optional) for which to choose a program for-- and the program is required to have this type of voice
   @return Program
   */
  protected Optional<Program> chooseFreshProgram(ProgramType programType, @Nullable InstrumentType voicingType) {
    var bag = MarbleBag.empty();

    // Retrieve programs bound to chain having a voice of the specified type
    Map<UUID/*ID*/, Program> programMap = fabricator.sourceMaterial()
      .getPrograms(programType).stream()
      .collect(Collectors.toMap(Program::getId, program -> program));
    Collection<Program> candidates = fabricator.sourceMaterial()
      .getProgramVoices().stream()
      .filter(programVoice -> Objects.nonNull(voicingType)
        && voicingType.equals(programVoice.getType())
        && programMap.containsKey(programVoice.getProgramId()))
      .map(ProgramVoice::getProgramId)
      .distinct()
      .map(programMap::get)
      .toList();

    // (3) score each source program based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;

    // Phase 1: Directly Bound Programs
    for (Program program : programsDirectlyBound(candidates)) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemesForProgramId(program.getId()));
      // FUTURE consider meme isometry, but for now, just use the meme stack
      if (iso.isAllowed(memes))
        bag.add(1, program.getId(), 1 + iso.score(memes));
    }

    // Phase 2: All Published Programs
    for (Program program : programsPublished(candidates)) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemesForProgramId(program.getId()));
      // FUTURE consider meme isometry, but for now, just use the meme stack
      if (iso.isAllowed(memes))
        bag.add(2, program.getId(), 1 + iso.score(memes));
    }

    // report
    fabricator.putReport(String.format("choiceOf%s%sProgram", voicingType, programType), bag.toString());

    // (4) return the top choice
    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getProgram(bag.pick());
  }

  /**
   Choose instrument
   [#325] Possible to choose multiple instruments for different voices in the same program
   <p>
   Choose drum instrument to fulfill beat program event names https://www.pivotaltracker.com/story/show/180803311

   @param type              of instrument to choose
   @param avoidIds          to avoid, or empty list
   @param continueVoiceName if present, ensure that choices continue for each voice named in prior segments of this main program
   @param requireEventNames instrument candidates are required to have event names https://www.pivotaltracker.com/story/show/180803311
   @return Instrument
   */
  protected Optional<Instrument> chooseFreshInstrument(InstrumentType type, Collection<UUID> avoidIds, @Nullable String continueVoiceName, List<String> requireEventNames) throws NexusException {
    var bag = MarbleBag.empty();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> candidates =
      fabricator.sourceMaterial().getInstruments(type)
        .stream()
        .filter(i -> !avoidIds.contains(i.getId()))
        .filter(i -> instrumentContainsAudioEventsLike(i, requireEventNames))
        .toList();

    // (3) score each source instrument based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;

    // Phase 1: Directly Bound Instruments
    for (Instrument instrument : instrumentsDirectlyBound(candidates)) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemesForInstrumentId(instrument.getId()));
      if (iso.isAllowed(memes))
        bag.add(1, instrument.getId(), 1 + iso.score(memes));
    }

    // Phase 2: All Published Instruments
    for (Instrument instrument : instrumentsPublished(candidates)) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemesForInstrumentId(instrument.getId()));
      if (iso.isAllowed(memes))
        bag.add(2, instrument.getId(), 1 + iso.score(memes));
    }

    // Instrument choice inertia: prefer same instrument choices throughout a main program
    // https://www.pivotaltracker.com/story/show/178442889
    if (SegmentType.CONTINUE == fabricator.getType()) {
      var alreadyPicked =
        fabricator.retrospective().getChoices().stream()
          .filter(candidate -> Objects.equals(candidate.getInstrumentType(), type.toString()))
          .filter(candidate -> Objects.nonNull(continueVoiceName))
          .filter(candidate -> fabricator.sourceMaterial().getProgramVoice(candidate.getProgramVoiceId())
            .stream().map(pv -> Objects.equals(continueVoiceName, pv.getName()))
            .findFirst()
            .orElse(false))
          .findAny();
      if (alreadyPicked.isPresent())
        return fabricator.sourceMaterial().getInstrument(alreadyPicked.get().getInstrumentId());
    }

    // report
    fabricator.putReport(String.format("choiceOf%sInstrument", type), bag.toString());

    // (4) return the top choice
    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrument(bag.pick());
  }

  /**
   PercLoop instrument audios are chosen in order of priority
   https://www.pivotaltracker.com/story/show/181262545
   <p>
   Choose drum instrument to fulfill beat program event names https://www.pivotaltracker.com/story/show/180803311

   @param type            of instrument to choose
   @param avoidIds        to avoid, or empty list
   @param preferredEvents instrument candidates are required to have event names https://www.pivotaltracker.com/story/show/180803311
   @return Instrument
   */
  @SuppressWarnings("SameParameterValue")
  protected Optional<InstrumentAudio> chooseFreshInstrumentAudio(InstrumentType type, Collection<UUID> avoidIds, List<String> preferredEvents) {
    var bag = MarbleBag.empty();

    // (2) retrieve instruments bound to chain
    Collection<InstrumentAudio> candidates =
      fabricator.sourceMaterial().getInstrumentAudios(type)
        .stream()
        .filter(a -> !avoidIds.contains(a.getId()))
        .toList();

    // (3) score each source instrument based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;

    // Phase 1: Directly Bound Audios (Preferred)
    for (InstrumentAudio audio : audiosDirectlyBound(candidates)) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemesForInstrumentId(audio.getInstrumentId()));
      if (iso.isAllowed(memes))
        bag.add(preferredEvents.contains(audio.getEvent()) ? 1 : 3, audio.getId(), 1 + iso.score(memes));
    }

    // Phase 2: All Published Audios (Preferred)
    for (InstrumentAudio audio : audiosPublished(candidates)) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemesForInstrumentId(audio.getInstrumentId()));
      if (iso.isAllowed(memes))
        bag.add(preferredEvents.contains(audio.getEvent()) ? 2 : 4, audio.getId(), 1 + iso.score(memes));
    }

    // report
    fabricator.putReport(String.format("choiceOf%sInstrument", type), bag.toString());

    // (4) return the top choice
    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

  /**
   Test if an instrument contains audios named like N
   <p>
   Choose drum instrument to fulfill beat program event names https://www.pivotaltracker.com/story/show/180803311

   @param instrument    to test
   @param requireEvents N
   @return true if instrument contains audios named like N or required event names list is empty
   */
  private boolean instrumentContainsAudioEventsLike(Instrument instrument, List<String> requireEvents) {
    if (requireEvents.isEmpty()) return true;
    for (var name : requireEvents)
      if (fabricator.sourceMaterial().getAudiosForInstrumentId(instrument.getId()).stream()
        .noneMatch(a -> 100 < NameIsometry.similarity(name, a.getEvent())))
        return false;
    return true;
  }

  /**
   Filter only the directly bound programs

   @param programs to filter
   @return filtered programs
   */
  protected Collection<Program> programsDirectlyBound(Collection<Program> programs) {
    return programs.stream().filter(fabricator::isDirectlyBound).toList();
  }

  /**
   Filter only the published programs

   @param programs to filter
   @return filtered programs
   */
  protected Collection<Program> programsPublished(Collection<Program> programs) {
    return programs.stream().filter(p -> ProgramState.Published.equals(p.getState())).toList();
  }

  /**
   Filter only the directly bound instruments

   @param instruments to filter
   @return filtered instruments
   */
  protected Collection<Instrument> instrumentsDirectlyBound(Collection<Instrument> instruments) {
    return instruments.stream().filter(fabricator::isDirectlyBound).toList();
  }

  /**
   Filter only the published instruments

   @param instruments to filter
   @return filtered instruments
   */
  protected Collection<Instrument> instrumentsPublished(Collection<Instrument> instruments) {
    return instruments.stream().filter(p -> InstrumentState.Published.equals(p.getState())).toList();
  }

  /**
   Filter only the directly bound instrumentAudios

   @param instrumentAudios to filter
   @return filtered instrumentAudios
   */
  protected Collection<InstrumentAudio> audiosDirectlyBound(Collection<InstrumentAudio> instrumentAudios) {
    return instrumentAudios.stream().filter(fabricator::isDirectlyBound).toList();
  }

  /**
   Filter only the published instrumentAudios

   @param instrumentAudios to filter
   @return filtered instrumentAudios
   */
  protected Collection<InstrumentAudio> audiosPublished(Collection<InstrumentAudio> instrumentAudios) {
    return instrumentAudios.stream().filter(a -> fabricator.sourceMaterial().getInstrument(a.getInstrumentId()).map(i -> InstrumentState.Published.equals(i.getState())).orElse(false)).toList();
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
    String get(SegmentChoice choice);
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
   Default choice index provider
   */
  public static class DefaultChoiceIndexProvider implements ChoiceIndexProvider {
    @Override
    public String get(SegmentChoice choice) {
      return choice.getId().toString();
    }
  }
}
