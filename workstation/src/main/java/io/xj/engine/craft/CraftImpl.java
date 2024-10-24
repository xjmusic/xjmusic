// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.craft;

import io.xj.engine.FabricationException;
import io.xj.engine.fabricator.FabricationWrapperImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.MemeIsometry;
import io.xj.engine.util.MarbleBag;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramState;
import io.xj.model.enums.ProgramType;
import io.xj.model.music.Accidental;
import io.xj.model.music.Bar;
import io.xj.model.music.Chord;
import io.xj.model.music.Note;
import io.xj.model.music.NoteRange;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentChordVoicing;
import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import io.xj.model.util.TremendouslyRandom;
import io.xj.model.util.ValueUtils;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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

import static io.xj.model.pojos.Segment.DELTA_UNLIMITED;
import static io.xj.model.util.ValueUtils.MICROS_PER_SECOND;

/**
 Arrangement of Segment Events is a common foundation for all craft
 */
public class CraftImpl extends FabricationWrapperImpl {
  final Map<String, Integer> deltaIns = new HashMap<>();
  final Map<String, Integer> deltaOuts = new HashMap<>();
  final List<InstrumentType> finalizeAudioLengthsForInstrumentTypes;
  ChoiceIndexProvider choiceIndexProvider = new DefaultChoiceIndexProvider();

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  public CraftImpl(Fabricator fabricator) {
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
   https://github.com/xjmusic/xjmusic/issues/233

   @param tempo              of main program
   @param sequence           for which to craft choices
   @param voices             for which to craft choices
   @param instrumentProvider from which to get instruments
   @throws FabricationException on failure
   */
  protected void craftNoteEvents(double tempo, ProgramSequence sequence, Collection<ProgramVoice> voices, InstrumentProvider instrumentProvider) throws FabricationException {
    // Craft each voice into choice
    for (ProgramVoice voice : voices) {
      var choice = new SegmentChoice();
      choice.setId(UUID.randomUUID());
      choice.setSegmentId(fabricator.getSegment().getId());
      choice.setMute(computeMute(voice.getType()));
      choice.setProgramType(fabricator.sourceMaterial().getProgram(voice.getProgramId()).orElseThrow(() -> new FabricationException("Can't get program for voice")).getType());
      choice.setInstrumentType(voice.getType());
      choice.setProgramId(voice.getProgramId());
      choice.setProgramSequenceId(sequence.getId());
      choice.setProgramVoiceId(voice.getId());

      // Whether there is a prior choice for this voice
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(voice);

      if (priorChoice.isPresent()) {
        choice.setDeltaIn(priorChoice.get().getDeltaIn());
        choice.setDeltaOut(priorChoice.get().getDeltaOut());
        choice.setInstrumentId(priorChoice.get().getInstrumentId());
        choice.setInstrumentMode(priorChoice.get().getInstrumentMode());
        var storedChoice = fabricator.put(choice, false);
        if (storedChoice.isPresent())
          this.craftNoteEventArrangements(tempo, storedChoice.get(), false);
        continue;
      }

      var instrument = instrumentProvider.get(voice);
      if (instrument.isEmpty()) {
        continue;
      }

      // make new choices
      choice.setDeltaIn(computeDeltaIn(choice));
      choice.setDeltaOut(computeDeltaOut(choice));
      choice.setInstrumentId(instrument.get().getId());
      choice.setInstrumentMode(instrument.get().getMode());
      var storedChoice = fabricator.put(choice, false);
      if (storedChoice.isPresent())
        this.craftNoteEventArrangements(tempo, storedChoice.get(), false);
    }
  }

  /**
   Chord instrument mode
   https://github.com/xjmusic/xjmusic/issues/235

   @param tempo      of main program
   @param instrument for which to craft choices
   @throws FabricationException on failure
   */
  protected void craftChordParts(double tempo, Instrument instrument) throws FabricationException {
    // Craft each voice into choice
    var choice = new SegmentChoice();

    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrument.getId());

    // Whether there is a prior choice for this voice
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(instrument.getType());

    if (priorChoice.isPresent()) {
      choice.setDeltaIn(priorChoice.get().getDeltaIn());
      choice.setDeltaOut(priorChoice.get().getDeltaOut());
      choice.setInstrumentId(priorChoice.get().getInstrumentId());
      var storedChoice = fabricator.put(choice, false);
      if (storedChoice.isPresent())
        this.craftChordParts(tempo, instrument, storedChoice.get());
      return;
    }

    // make new choices
    choice.setDeltaIn(computeDeltaIn(choice));
    choice.setDeltaOut(computeDeltaOut(choice));
    choice.setInstrumentId(instrument.getId());
    var storedChoice = fabricator.put(choice, false);
    if (storedChoice.isPresent())
      this.craftChordParts(tempo, instrument, storedChoice.get());
  }

  /**
   Chord instrument mode
   https://github.com/xjmusic/xjmusic/issues/235

   @param tempo      of main program
   @param instrument chosen
   @param choice     for which to craft chord parts
   @throws FabricationException on failure
   */
  protected void craftChordParts(double tempo, Instrument instrument, SegmentChoice choice) throws FabricationException {
    if (fabricator.getSegmentChords().isEmpty()) return;

    // Arrangement
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(choice.getSegmentId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Pick for each section
    for (var section : computeSections()) {
      var audio = selectChordPartInstrumentAudio(instrument, Chord.of(section.chord.getName()));

      // Should gracefully skip audio in unfulfilled by instrument https://github.com/xjmusic/xjmusic/issues/240
      if (audio.isEmpty()) continue;

      // Pick attributes are expressed "rendered" as actual seconds
      long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, section.fromPos);
      long lengthMicros = fabricator.isOneShot(instrument) ? (long) (audio.get().getLengthSeconds() * MICROS_PER_SECOND) : fabricator.getSegmentMicrosAtPosition(tempo, section.toPos) - startAtSegmentMicros;

      // Volume ratio
      var volRatio = computeVolumeRatioForPickedNote(choice, section.fromPos);
      if (0 >= volRatio) continue;

      // Pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(choice.getSegmentId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setInstrumentAudioId(audio.get().getId());
      pick.setStartAtSegmentMicros(startAtSegmentMicros);
      pick.setTones(section.chord.getName());
      pick.setEvent(StringUtils.toEvent(instrument.getType().toString()));
      pick.setLengthMicros(lengthMicros);
      pick.setAmplitude(volRatio);
      fabricator.put(pick);
    }

    // Final pass to set the actual length of one-shot audio picks
    finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choice);
  }

  /**
   Event instrument mode

   @param tempo      of main program
   @param instrument for which to craft choices
   @param program    for which to craft choices
   @throws FabricationException on failure
   */
  protected void craftEventParts(double tempo, Instrument instrument, Program program) throws FabricationException {
    // Event detail sequence is selected at random of the current instrument
    // FUTURE: Detail Instrument with multiple Sequences https://github.com/xjmusic/xjmusic/issues/241
    var sequence = fabricator.getRandomlySelectedSequence(program);

    // Event voice arrangements
    if (sequence.isPresent()) {
      var voices = fabricator.sourceMaterial().getVoicesOfProgram(program);
      if (voices.isEmpty()) return;
      craftNoteEvents(tempo, sequence.get(), voices, ignored -> Optional.of(instrument));
    }
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
   https://github.com/xjmusic/xjmusic/issues/242
   Perform the inertia analysis, and determine whether they actually use the new choice or not
   IMPORTANT** If the previously chosen instruments are for the previous main program as the current segment,
   the inertia scores are not actually added to the regular scores or used to make choices--
   this would prevent new choices from being made. **Inertia must be its own layer of calculation,
   a question of whether the choices will be followed or whether the inertia will be followed**
   thus the new choices have been made, we know *where* we're going next,
   but we aren't actually using them yet until we hit the next main program in full, N segments later.
   <p>
   Ends with a final pass to set the actual length of one-shot audio picks
   One-shot instruments cut off when other notes played with same instrument, or at end of segment https://github.com/xjmusic/xjmusic/issues/243

   @param tempo         of main program
   @param choice        to craft arrangements for
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws FabricationException on failure
   */
  protected void craftNoteEventArrangements(double tempo, SegmentChoice choice, boolean defaultAtonal) throws FabricationException {
    // this is used to invert voicings into the tightest possible range
    // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
    NoteRange range = NoteRange.empty();

    var programConfig = fabricator.getProgramConfig(fabricator.getProgram(choice).orElseThrow(() -> new FabricationException("Can't get program config")));

    if (fabricator.getSegmentChords().isEmpty())
      craftNoteEventSection(tempo, choice, 0, fabricator.getSegment().getTotal(), range, defaultAtonal);

    else if (programConfig.doPatternRestartOnChord())
      craftNoteEventSectionRestartingEachChord(tempo, choice, range, defaultAtonal);

    else craftNoteEventSection(tempo, choice, 0, fabricator.getSegment().getTotal(), range, defaultAtonal);

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
   https://github.com/xjmusic/xjmusic/issues/233
   <p>
   Shift deltas so 2x more time is spent on construction than deconstruction
   <p>
   Vary the high plateau between delta in and out across layers

   @throws FabricationException on failure
   */
  protected void precomputeDeltas(Predicate<SegmentChoice> choiceFilter, ChoiceIndexProvider choiceIndexProvider, Collection<String> layers, Collection<String> layerPrioritizationSearches, int numLayersIncoming) throws FabricationException {
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

      case INITIAL, NEXT_MAIN, NEXT_MACRO -> {
        // randomly override N incoming (deltaIn unlimited) and N outgoing (deltaOut unlimited)
        // shuffle the layers into a random order, then step through them, assigning delta ins and then outs
        // random order in
        var barBeats = fabricator.getCurrentMainProgramConfig().getBarBeats();
        var deltaUnits = Bar.of(barBeats).computeSubsectionBeats(fabricator.getSegment().getTotal());

        // Delta arcs can prioritize the presence of a layer by name, e.g. containing "kick"
        // separate layers into primary and secondary, shuffle them separately, then concatenate
        List<String> priLayers = new ArrayList<>();
        List<String> secLayers = new ArrayList<>();
        layers.forEach(layer -> {
          var layerName = layer.toLowerCase(Locale.ROOT);
          if (layerPrioritizationSearches.stream().anyMatch(m -> layerName.contains(m.toLowerCase(Locale.ROOT))))
            priLayers.add(layer);
          else secLayers.add(layer);
        });
        Collections.shuffle(priLayers);
        if (!priLayers.isEmpty()) fabricator.addInfoMessage(String.format("Prioritized %s", CsvUtils.join(priLayers)));
        Collections.shuffle(secLayers);
        var orderedLayers = Stream.concat(priLayers.stream(), secLayers.stream()).toList();
        var delta = ValueUtils.roundToNearest(deltaUnits, TremendouslyRandom.zeroToLimit(deltaUnits * 4) - deltaUnits * 2 * numLayersIncoming);
        for (String orderedLayer : orderedLayers) {
          deltaIns.put(orderedLayer, delta > 0 ? delta : DELTA_UNLIMITED);
          deltaOuts.put(orderedLayer, DELTA_UNLIMITED); // all layers get delta out unlimited
          delta += ValueUtils.roundToNearest(deltaUnits, TremendouslyRandom.zeroToLimit(deltaUnits * 5));
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
   Detail programs can be made to repeat every chord change https://github.com/xjmusic/xjmusic/issues/244

   @param tempo         of main program
   @param choice        from which to craft events
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws FabricationException on failure
   */
  void craftNoteEventSectionRestartingEachChord(double tempo, SegmentChoice choice, NoteRange range, boolean defaultAtonal) throws FabricationException {
    for (var section : computeSections())
      craftNoteEventSection(tempo, choice, section.fromPos, section.toPos, range, defaultAtonal);
  }

  /**
   Compute the segment chord sections

   @return sections in order of position ascending
   */
  List<Section> computeSections() {
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
      sections[i].toPos = i < chords.length - 1 ? chords[i + 1].getPosition() : fabricator.getSegment().getTotal();
    }
    return Arrays.stream(sections).toList();
  }

  /**
   Craft events for a section of one detail voice

   @param tempo         of main program
   @param choice        from which to craft events
   @param fromPos       position (in beats)
   @param maxPos        position (in beats)
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @throws FabricationException on failure
   */
  void craftNoteEventSection(double tempo, SegmentChoice choice, double fromPos, double maxPos, NoteRange range, boolean defaultAtonal) throws FabricationException {

    // begin at the beginning and fabricate events for the segment of beginning to end
    double curPos = fromPos;

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < maxPos) {
      Optional<ProgramSequencePattern> loopPattern = fabricator.getRandomlySelectedPatternOfSequenceByVoiceAndType(choice);
      if (loopPattern.isPresent())
        curPos += craftPatternEvents(tempo, choice, loopPattern.get(), curPos, maxPos, range, defaultAtonal);
      else curPos = maxPos;
    }
  }

  /**
   Craft the voice events of a single pattern.
   Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

   @param tempo         of main program
   @param pattern       to source events
   @param fromPosition  to write events to segment
   @param toPosition    to write events to segment
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   @return deltaPos of start, after crafting this batch of pattern events
   */
  double craftPatternEvents(double tempo, SegmentChoice choice, ProgramSequencePattern pattern, double fromPosition, double toPosition, NoteRange range, boolean defaultAtonal) throws FabricationException {
    if (Objects.isNull(pattern)) throw new FabricationException("Cannot craft create null pattern");
    double loopBeats = toPosition - fromPosition;
    List<ProgramSequencePatternEvent> events = fabricator.sourceMaterial().getEventsOfPattern(pattern);

    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(choice.getSegmentId());
    arrangement.segmentChoiceId(choice.getId());
    arrangement.setProgramSequencePatternId(pattern.getId());
    fabricator.put(arrangement);

    var instrument = fabricator.sourceMaterial().getInstrument(choice.getInstrumentId()).orElseThrow(() -> new FabricationException("Failed to retrieve instrument"));
    for (ProgramSequencePatternEvent event : events)
      pickNotesAndInstrumentAudioForEvent(tempo, instrument, choice, arrangement, fromPosition, toPosition, event, range, defaultAtonal);
    return Math.min(loopBeats, pattern.getTotal());
  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param tempo

   @param choice        to pick notes for
   @param fromPosition  to pick notes for
   @param toPosition    to pick notes for
   @param event         to pick audio for
   @param range         used to keep voicing in the tightest range possible
   @param defaultAtonal whether to default to a single atonal note, if no voicings are available
   */
  void pickNotesAndInstrumentAudioForEvent(double tempo, Instrument instrument, SegmentChoice choice, SegmentChoiceArrangement arrangement, double fromPosition, double toPosition, ProgramSequencePatternEvent event, NoteRange range, boolean defaultAtonal) throws FabricationException {
    // Segment position is expressed in beats
    double segmentPosition = fromPosition + event.getPosition();

    // Should never place segment events outside of segment time range
    if (segmentPosition < 0 || segmentPosition >= fabricator.getSegment().getTotal()) return;

    double duration = Math.min(event.getDuration(), toPosition - segmentPosition);
    var chord = fabricator.getChordAt(segmentPosition);
    Optional<SegmentChordVoicing> voicing = chord.isPresent() ? fabricator.chooseVoicing(chord.get(), instrument.getType()) : Optional.empty();

    var volRatio = computeVolumeRatioForPickedNote(choice, segmentPosition);
    if (0 >= volRatio) return;

    // The final note is voiced from the chord voicing (if found) or else the default is used
    Set<String> notes = voicing.isPresent() ? pickNotesForEvent(instrument.getType(), choice, event, chord.get(), voicing.get(), range) : (defaultAtonal ? Set.of(Note.ATONAL) : Set.of());

    // Pick attributes are expressed "rendered" as actual seconds
    long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, segmentPosition);
    @Nullable Long lengthMicros = fabricator.isOneShot(instrument, fabricator.getTrackName(event)) ? null : fabricator.getSegmentMicrosAtPosition(tempo, segmentPosition + duration) - startAtSegmentMicros;

    // pick an audio for each note
    for (var note : notes)
      pickInstrumentAudio(note, instrument, event, arrangement, startAtSegmentMicros, lengthMicros, voicing.map(SegmentChordVoicing::getId).orElse(null), volRatio);
  }

  /**
   Ends with a final pass to set the actual length of one-shot audio picks
   One-shot instruments cut off when other notes played with same instrument, or at end of segment https://github.com/xjmusic/xjmusic/issues/243

   @param choice for which to finalize length of one-shot audio picks
   */
  void finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(SegmentChoice choice) throws FabricationException {
    var instrument = fabricator.sourceMaterial().getInstrument(choice.getInstrumentId()).orElseThrow(() -> new FabricationException("Failed to get instrument from source material for segment choice!"));

    // skip instruments that are not one-shot
    if (!fabricator.isOneShot(instrument)) return;

    // skip instruments that are do not have one-shot cutoff enabled https://github.com/xjmusic/xjmusic/issues/225
    if (!fabricator.isOneShotCutoffEnabled(instrument)) return;

    // skip instruments that are not on the list
    if (!finalizeAudioLengthsForInstrumentTypes.contains(instrument.getType())) return;

    // get all the picks, ordered chronologically, and skip the rest of this process if there are none
    List<SegmentChoiceArrangementPick> picks = fabricator.getPicks(choice);
    if (picks.isEmpty()) return;

    // build an ordered unique list of the moments in time when the one-shot will be cut off
    List<Long> cutoffAtSegmentMicros = picks.stream().map(SegmentChoiceArrangementPick::getStartAtSegmentMicros).collect(Collectors.toSet()).stream().sorted().toList();

    // iterate and set lengths of all picks in series
    for (SegmentChoiceArrangementPick pick : picks) {

      // Skip picks that already have their end length set
      if (Objects.nonNull(pick.getLengthMicros())) continue;

      var nextCutoffAtSegmentMicros = cutoffAtSegmentMicros.stream().filter(c -> c > pick.getStartAtSegmentMicros()).findFirst();

      if (nextCutoffAtSegmentMicros.isPresent()) {
        pick.setLengthMicros(nextCutoffAtSegmentMicros.get() - pick.getStartAtSegmentMicros());
        fabricator.put(pick);
        continue;
      }

      if (pick.getStartAtSegmentMicros() < fabricator.getTotalSegmentMicros()) {
        pick.setLengthMicros(fabricator.getTotalSegmentMicros() - pick.getStartAtSegmentMicros());
        fabricator.put(pick);
        continue;
      }

      fabricator.delete(pick.getSegmentId(), SegmentChoiceArrangementPick.class, pick.getId());
    }
  }

  /**
   Compute the volume ratio of a picked note

   @param choice          for which to compute volume ratio
   @param segmentPosition at which to compute
   @return volume ratio
   */
  float computeVolumeRatioForPickedNote(SegmentChoice choice, double segmentPosition) {
    if (!fabricator.getTemplateConfig().isDeltaArcEnabled()) return 1.0f;
    return (float) (inBounds(choice.getDeltaIn(), choice.getDeltaOut(), fabricator.getSegment().getDelta() + segmentPosition) ? 1.0 : 0.0);
  }

  /**
   Whether the current segment contains the delta in for the given choice

   @param choice to test whether the current segment contains this choice delta in
   @return true if the current segment contains the given choice's delta in
   */
  public boolean isIntroSegment(SegmentChoice choice) {
    return !isUnlimitedIn(choice) && choice.getDeltaIn() >= fabricator.getSegment().getDelta() && choice.getDeltaIn() < fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal();
  }

  /**
   Whether the current segment contains the delta out for the given choice

   @param choice to test whether the current segment contains this choice delta out
   @return true if the current segment contains the given choice's delta out
   */
  public boolean isOutroSegment(SegmentChoice choice) {
    return !isUnlimitedOut(choice) && choice.getDeltaOut() <= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal() && choice.getDeltaOut() > fabricator.getSegment().getDelta();
  }

  /**
   Whether the given choice is silent during the entire segment

   @param choice to test for silence
   @return true if choice is silent the entire segment
   */
  public boolean isSilentEntireSegment(SegmentChoice choice) {
    return (choice.getDeltaOut() < fabricator.getSegment().getDelta()) || (choice.getDeltaIn() >= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal());
  }

  /**
   Whether the given choice is fully active during the current segment

   @param choice to test for activation
   @return true if this choice is active the entire time
   */
  public boolean isActiveEntireSegment(SegmentChoice choice) {
    return (choice.getDeltaIn() <= fabricator.getSegment().getDelta()) && (choice.getDeltaOut() >= fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal());
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   XJ should choose correct instrument note based on detail program note

   @param instrumentType  comprising audios
   @param choice          for reference
   @param event           of program to pick instrument note for
   @param rawSegmentChord to use for interpreting the voicing
   @param voicing         to choose a note from
   @param optimalRange    used to keep voicing in the tightest range possible
   @return note picked from the available voicing
   */
  Set<String> pickNotesForEvent(InstrumentType instrumentType, SegmentChoice choice, ProgramSequencePatternEvent event, SegmentChord rawSegmentChord, SegmentChordVoicing voicing, NoteRange optimalRange) throws FabricationException {
    // Various computations to prepare for picking
    var segChord = Chord.of(rawSegmentChord.getName());
    var dpKey = fabricator.getKeyForChoice(choice);
    var dpRange = fabricator.getProgramRange(choice.getProgramId(), instrumentType);
    var voicingListRange = fabricator.getProgramVoicingNoteRange(instrumentType);

    // take semitone shift into project before computing octave shift! https://github.com/xjmusic/xjmusic/issues/245
    var dpTransposeSemitones = fabricator.getProgramTargetShift(instrumentType, dpKey, segChord);
    var dpTransposeOctaveSemitones = 12 * fabricator.getProgramRangeShiftOctaves(instrumentType, dpRange.shifted(dpTransposeSemitones), voicingListRange);

    // Event notes are either interpreted from specific notes in dp, or via sticky bun from X notes in dp
    List<Note> eventNotes = CsvUtils.split(event.getTones()).stream().map(n -> Note.of(n).shift(dpTransposeSemitones + dpTransposeOctaveSemitones)).sorted().collect(Collectors.toList());
    var dpEventRelativeOffsetWithinRangeSemitones = dpRange.shifted(dpTransposeSemitones + dpTransposeOctaveSemitones).getDeltaSemitones(NoteRange.ofNotes(eventNotes));
    var dpEventRangeWithinWholeDP = NoteRange.ofNotes(eventNotes).shifted(dpEventRelativeOffsetWithinRangeSemitones);

    if (optimalRange.isEmpty() && !dpEventRangeWithinWholeDP.isEmpty())
      optimalRange.expand(dpEventRangeWithinWholeDP);

    // Leverage segment meta to look up a sticky bun if it exists
    var bun = fabricator.getStickyBun(event.getId());

    // Prepare voicing notes and note picker
    var voicingNotes = fabricator.getNotes(voicing).stream().flatMap(Note::ofValid).collect(Collectors.toList());
    var notePicker = new NotePicker(optimalRange.shifted(dpEventRelativeOffsetWithinRangeSemitones), voicingNotes, fabricator.getTemplateConfig().getInstrumentTypesForInversionSeeking().contains(instrumentType));

    // Go through the notes in the event and pick a note from the voicing, either by note picker or by sticky bun
    List<Note> pickedNotes = new ArrayList<>();
    for (var i = 0; i < eventNotes.size(); i++) {
      var pickedNote = eventNotes.get(i).isAtonal() && bun.isPresent() ? bun.get().compute(voicingNotes, i) : notePicker.pick(eventNotes.get(i));
      pickedNotes.add(pickedNote);
    }

    var pickedNoteStrings = pickedNotes.stream().map(n -> n.toString(segChord.getAdjSymbol())).collect(Collectors.toSet());

    // expand the optimal range for voice leading by the notes that were just picked
    optimalRange.expand(pickedNotes);

    // outcome
    return pickedNoteStrings;
  }

  /**
   XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
   <p>
   Artist can edit comma-separated notes into detail program events https://github.com/xjmusic/xjmusic/issues/246
   <p>
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param note                     to pick audio for
   @param instrument               from which to pick audio
   @param event                    to pick audio for
   @param segmentChoiceArrangement arranging this instrument for a program
   @param startAtSegmentMicros     of audio
   @param lengthMicros             of audio
   @param volRatio                 ratio of volume
   @throws FabricationException on failure
   */
  void pickInstrumentAudio(
    String note,
    Instrument instrument,
    ProgramSequencePatternEvent event,
    SegmentChoiceArrangement segmentChoiceArrangement,
    Long startAtSegmentMicros,
    @Nullable Long lengthMicros,
    @Nullable UUID segmentChordVoicingId,
    float volRatio
  ) throws FabricationException {
    var audio = fabricator.getInstrumentConfig(instrument).isMultiphonic() ? selectMultiphonicInstrumentAudio(instrument, event, note) : selectMonophonicInstrumentAudio(instrument, event);

    // Should gracefully skip audio if unfulfilled by instrument https://github.com/xjmusic/xjmusic/issues/240
    if (audio.isEmpty()) return;

    // of pick
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(segmentChoiceArrangement.getSegmentId());
    pick.setSegmentChoiceArrangementId(segmentChoiceArrangement.getId());
    pick.setInstrumentAudioId(audio.get().getId());
    pick.setProgramSequencePatternEventId(event.getId());
    pick.setEvent(fabricator.getTrackName(event));
    pick.setStartAtSegmentMicros(startAtSegmentMicros);
    pick.setLengthMicros(lengthMicros);
    pick.setAmplitude(event.getVelocity() * volRatio);
    pick.setTones(fabricator.getInstrumentConfig(instrument).isTonal() ? note : Note.ATONAL);
    if (Objects.nonNull(segmentChordVoicingId)) pick.setSegmentChordVoicingId(segmentChordVoicingId);
    fabricator.put(pick);
  }

  /**
   Pick the transition

   @param arrangement          to pick
   @param audio                to pick
   @param startAtSegmentMicros to pick
   @param lengthMicros         to pick
   @param event                to pick
   @throws FabricationException on failure
   */
  protected void pickInstrumentAudio(
    SegmentChoiceArrangement arrangement,
    InstrumentAudio audio,
    long startAtSegmentMicros,
    long lengthMicros,
    String event
  ) throws FabricationException {
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(fabricator.getSegment().getId());
    pick.setSegmentChoiceArrangementId(arrangement.getId());
    pick.setStartAtSegmentMicros(startAtSegmentMicros);
    pick.setLengthMicros(lengthMicros);
    pick.setEvent(event);
    pick.setAmplitude((float) 1.0);
    pick.setInstrumentAudioId(audio.getId());
    fabricator.put(pick);
  }

  /**
   Select audios for the given instrument

   @param instrument for which to pick audio
   @return drum-type Instrument
   */
  protected Collection<InstrumentAudio> selectGeneralAudioIntensityLayers(Instrument instrument) {
    var previous = fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId());
    if (!previous.isEmpty() && fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
      return previous.stream()
        .map(SegmentChoiceArrangementPick::getInstrumentAudioId)
        .collect(Collectors.toSet()) // unique audio ids
        .stream()
        .map(audioId -> fabricator.sourceMaterial().getInstrumentAudio(audioId))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
    }

    return selectAudioIntensityLayers(
      fabricator.sourceMaterial().getAudiosOfInstrument(instrument.getId()),
      fabricator.getTemplateConfig().getIntensityLayers(instrument.getType())
    );
  }

  /**
   Pick one audio for each desired intensity level, by layering the audios by intensity and picking one from each layer.
   Divide the audios into layers (ergo grouping them by intensity ascending) and pick one audio per layer.

   @param audios from which to pick layers
   @param layers number of layers to pick
   @return picked audios
   */
  protected Collection<InstrumentAudio> selectAudioIntensityLayers(Collection<InstrumentAudio> audios, int layers) {
    List<InstrumentAudio> sorted = audios.stream()
      .sorted(Comparator.comparing(InstrumentAudio::getIntensity))
      .toList();
    if (sorted.isEmpty()) return Set.of();

    // Create a list of bags, one for each layer
    List<MarbleBag> bags = Stream.iterate(0, i -> i < layers, i -> i + 1).map(i -> MarbleBag.empty()).toList();

    // Iterate through the available audios, and add them to the bags, divided into the number of layers
    int marblesPerLayer = (int) Math.ceil((double) sorted.size() / layers);
    if (marblesPerLayer == 0) return Set.of();
    for (int i = 0; i < sorted.size(); i++) {
      bags.get(i / marblesPerLayer).add(1, sorted.get(i).getId());
    }

    return bags.stream().flatMap((bag) -> {
      if (bag.isEmpty()) return Stream.empty();
      return fabricator.sourceMaterial().getInstrumentAudio(bag.pick()).stream();
    }).collect(Collectors.toSet());
  }

  /**
   Select audio from a multiphonic instrument
   <p>
   Sampler obeys isMultiphonic from Instrument config https://github.com/xjmusic/xjmusic/issues/252

   @param instrument of which to score available audios, and make a selection
   @param event      for caching reference
   @param note       to match selection
   @return matched new audio
   */
  Optional<InstrumentAudio> selectMultiphonicInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event, String note) {
    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
      if (fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), note).isEmpty()) {
        var audio = selectNewMultiphonicInstrumentAudio(instrument, note);
        audio.ifPresent(instrumentAudio -> fabricator.putPreferredAudio(event.getProgramVoiceTrackId().toString(), note, instrumentAudio));
      }
      return fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), note);

    } else {
      return selectNewMultiphonicInstrumentAudio(instrument, note);
    }
  }

  /**
   Select audio from a multiphonic instrument
   <p>
   If never encountered, default to new selection and cache that.

   @param instrument of which to score available audios, and make a selection
   @param event      to match selection
   @return matched new audio
   @throws FabricationException on failure
   */
  Optional<InstrumentAudio> selectMonophonicInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event) throws FabricationException {
    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
      if (fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones()).isEmpty())
        fabricator.putPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones(), selectNewNoteEventInstrumentAudio(instrument, event).orElseThrow(() -> new FabricationException("Unable to select note event instrument audio!")));
      return fabricator.getPreferredAudio(event.getProgramVoiceTrackId().toString(), event.getTones());

    } else {
      return selectNewNoteEventInstrumentAudio(instrument, event);
    }
  }

  /**
   Chord instrument mode
   https://github.com/xjmusic/xjmusic/issues/235
   <p>
   If never encountered, default to new selection and cache that.

   @param instrument of which to score available audios, and make a selection
   @param chord      to match selection
   @return matched new audio
   */
  Optional<InstrumentAudio> selectChordPartInstrumentAudio(Instrument instrument, Chord chord) {
    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent()) {
      if (fabricator.getPreferredAudio(instrument.getId().toString(), chord.getName()).isEmpty()) {
        var audio = selectNewChordPartInstrumentAudio(instrument, chord);
        audio.ifPresent(instrumentAudio -> fabricator.putPreferredAudio(instrument.getId().toString(), chord.getName(), instrumentAudio));
      }
      return fabricator.getPreferredAudio(instrument.getId().toString(), chord.getName());

    } else {
      return selectNewChordPartInstrumentAudio(instrument, chord);
    }
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   @return matched new audio
   */
  Optional<InstrumentAudio> selectNewNoteEventInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event) throws FabricationException {
    Map<UUID, Integer> score = new HashMap<>();

    // add all audio to chooser
    fabricator.sourceMaterial().getAudiosOfInstrument(instrument).forEach(a -> score.put(a.getId(), 0));

    // score each audio against the current voice event, with some variability
    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosOfInstrument(instrument))
      if (instrument.getType() == InstrumentType.Drum)
        score.put(audio.getId(), Objects.equals(fabricator.getTrackName(event), audio.getEvent()) ? 300 : 0);
      else if (Note.of(audio.getTones()).sameAs(Note.of(event.getTones())))
        score.put(audio.getId(), 100);

    // final chosen audio event
    var pickId = ValueUtils.getKeyOfHighestValue(score);
    return pickId.isPresent() ? fabricator.sourceMaterial().getInstrumentAudio(pickId.get()) : Optional.empty();
  }


  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param chord      to match
   @return matched new audio
   */
  protected Optional<InstrumentAudio> selectNewChordPartInstrumentAudio(Instrument instrument, Chord chord) {
    var bag = MarbleBag.empty();

    Chord audioChord;
    for (var a : fabricator.sourceMaterial().getAudiosOfInstrument(instrument)) {
      audioChord = Chord.of(a.getTones());
      if (audioChord.isSame(chord)) {
        bag.add(0, a.getId());
      } else if (audioChord.isAcceptable(chord)) {
        bag.add(1, a.getId());
      }
    }

    if (bag.isEmpty()) return Optional.empty();

    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

  /**
   Select a new random instrument audio based on a pattern event
   <p>
   Sampler obeys isMultiphonic from Instrument config https://github.com/xjmusic/xjmusic/issues/252

   @param instrument of which to score available audios, and make a selection
   @param note       to match
   @return matched new audio
   */
  Optional<InstrumentAudio> selectNewMultiphonicInstrumentAudio(Instrument instrument, String note) {
    var instrumentAudios = fabricator.sourceMaterial().getAudiosOfInstrument(instrument);
    var a = Note.of(note);
    var audio = MarbleBag.quickPick(instrumentAudios.stream().filter(candidate -> {
      if (Objects.isNull(candidate) || StringUtils.isNullOrEmpty(candidate.getTones())) return false;
      var b = Note.of(candidate.getTones());
      return a.isAtonal() || b.isAtonal() || a.sameAs(b);
    }).toList());

    if (audio.isEmpty()) {
      reportMissing(Map.of("instrumentId", instrument.getId().toString(), "searchForNote", note, "availableNotes", CsvUtils.from(instrumentAudios.stream().map(InstrumentAudio::getTones).map(Note::of).sorted(Note::compareTo).map(N -> N.toString(Accidental.Sharp)).collect(Collectors.toList()))));
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
    Map<UUID/*ID*/, Program> programMap = fabricator.sourceMaterial().getProgramsOfType(programType).stream().collect(Collectors.toMap(Program::getId, program -> program));
    Collection<Program> candidates = fabricator.sourceMaterial().getProgramVoices().stream().filter(programVoice -> Objects.nonNull(voicingType) && voicingType.equals(programVoice.getType()) && programMap.containsKey(programVoice.getProgramId())).map(ProgramVoice::getProgramId).distinct().map(programMap::get).toList();

    // (3) score each source program based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;

    // Phase 1: Directly Bound Programs
    for (Program program : programsDirectlyBound(candidates)) {
      memes = EntityUtils.namesOf(fabricator.sourceMaterial().getMemesOfProgram(program.getId()));
      // FUTURE consider meme isometry, but for now, just use the meme stack
      if (iso.isAllowed(memes)) bag.add(1, program.getId(), 1 + iso.score(memes));
    }

    // Phase 2: All Published Programs
    for (Program program : programsPublished(candidates)) {
      memes = EntityUtils.namesOf(fabricator.sourceMaterial().getMemesOfProgram(program.getId()));
      // FUTURE consider meme isometry, but for now, just use the meme stack
      if (iso.isAllowed(memes)) bag.add(2, program.getId(), 1 + iso.score(memes));
    }

    // report
    fabricator.putReport(String.format("choiceOf%s%sProgram", voicingType, programType), bag.toString());

    // (4) return the top choice
    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getProgram(bag.pick());
  }

  /**
   Choose instrument
   <p>
   Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

   @param type              of instrument to choose from
   @param requireEventNames instrument candidates are required to have event names https://github.com/xjmusic/xjmusic/issues/253
   @return Instrument
   */
  protected Optional<Instrument> chooseFreshInstrument(InstrumentType type, Collection<String> requireEventNames) {
    var bag = MarbleBag.empty();

    // Retrieve instruments bound to chain
    Collection<Instrument> candidates = fabricator.sourceMaterial().getInstrumentsOfType(type).stream().filter(i -> instrumentContainsAudioEventsLike(i, requireEventNames)).toList();

    // Retrieve meme isometry of segment
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;

    // Phase 1: Directly Bound Instruments
    for (Instrument instrument : instrumentsDirectlyBound(candidates)) {
      memes = EntityUtils.namesOf(fabricator.sourceMaterial().getMemesOfInstrument(instrument.getId()));
      if (iso.isAllowed(memes)) bag.add(1, instrument.getId(), 1 + iso.score(memes));
    }

    // Phase 2: All Published Instruments
    for (Instrument instrument : instrumentsPublished(candidates)) {
      memes = EntityUtils.namesOf(fabricator.sourceMaterial().getMemesOfInstrument(instrument.getId()));
      if (iso.isAllowed(memes)) bag.add(2, instrument.getId(), 1 + iso.score(memes));
    }

    // report
    fabricator.putReport(String.format("choiceOf%sInstrument", type), bag.toString());

    // (4) return the top choice
    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrument(bag.pick());
  }

  /**
   Percussion-type Loop-mode instrument audios are chosen in order of priority
   https://github.com/xjmusic/xjmusic/issues/255
   <p>
   Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

   @param types           of instrument to choose from
   @param modes           of instrument to choose from
   @param avoidIds        to avoid, or empty list
   @param preferredEvents instrument candidates are required to have event names https://github.com/xjmusic/xjmusic/issues/253
   @return Instrument
   */
  @SuppressWarnings("SameParameterValue")
  protected Optional<InstrumentAudio> chooseFreshInstrumentAudio(Collection<InstrumentType> types, Collection<InstrumentMode> modes, Collection<UUID> avoidIds, Collection<String> preferredEvents) {
    var bag = MarbleBag.empty();

    // (2) retrieve instruments bound to chain
    Collection<InstrumentAudio> candidates = fabricator.sourceMaterial().getAudiosOfInstrumentTypesAndModes(types, modes).stream().filter(a -> !avoidIds.contains(a.getId())).toList();

    // (3) score each source instrument based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;

    // Phase 1: Directly Bound Audios (Preferred)
    for (InstrumentAudio audio : audiosDirectlyBound(candidates)) {
      memes = EntityUtils.namesOf(fabricator.sourceMaterial().getMemesOfInstrument(audio.getInstrumentId()));
      if (iso.isAllowed(memes))
        bag.add(preferredEvents.contains(audio.getEvent()) ? 1 : 3, audio.getId(), 1 + iso.score(memes));
    }

    // Phase 2: All Published Audios (Preferred)
    for (InstrumentAudio audio : audiosPublished(candidates)) {
      memes = EntityUtils.namesOf(fabricator.sourceMaterial().getMemesOfInstrument(audio.getInstrumentId()));
      if (iso.isAllowed(memes))
        bag.add(preferredEvents.contains(audio.getEvent()) ? 2 : 4, audio.getId(), 1 + iso.score(memes));
    }

    // report
    fabricator.putReport(String.format("choice%s%s", types.stream().map(InstrumentType::toString).collect(Collectors.joining()), modes.stream().map(InstrumentMode::toString).collect(Collectors.joining())), bag.toString());

    // (4) return the top choice
    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
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
   Compute a mute value, based on the template config

   @param instrumentType of instrument for which to compute mute
   @return true if muted
   */
  protected boolean computeMute(InstrumentType instrumentType) {
    return TremendouslyRandom.booleanChanceOf(fabricator.getTemplateConfig().getChoiceMuteProbability(instrumentType));
  }

  /**
   Test if an instrument contains audios named like N
   <p>
   Choose drum instrument to fulfill beat program event names https://github.com/xjmusic/xjmusic/issues/253

   @param instrument    to test
   @param requireEvents N
   @return true if instrument contains audios named like N or required event names list is empty
   */
  boolean instrumentContainsAudioEventsLike(Instrument instrument, Collection<String> requireEvents) {
    if (requireEvents.isEmpty()) return true;
    for (var event : requireEvents)
      if (fabricator.sourceMaterial().getAudiosOfInstrument(instrument.getId()).stream().noneMatch(a -> Objects.equals(event, a.getEvent())))
        return false;
    return true;
  }

  /**
   Instrument provider to make some code more portable
   */
  public interface InstrumentProvider {

    Optional<Instrument> get(ProgramVoice voice) throws FabricationException;
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
    public Double fromPos;
    public Double toPos;
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
