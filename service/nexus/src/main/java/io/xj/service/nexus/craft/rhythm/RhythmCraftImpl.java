// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.rhythm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import datadog.trace.api.Trace;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Chance;
import io.xj.service.hub.client.HubClientException;
import io.xj.service.nexus.craft.CraftException;
import io.xj.service.nexus.craft.detail.DetailCraftImpl;
import io.xj.service.nexus.fabricator.EntityScorePicker;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 Rhythm craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 [#176625174] RhythmCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class RhythmCraftImpl extends DetailCraftImpl implements RhythmCraft {
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private static final double SCORE_DIRECTLY_BOUND = 100;
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);

  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "doWork")
  public void doWork() throws CraftException {
    try {

      // program
      Optional<Program> rhythmProgram = chooseRhythmProgram();
      if (rhythmProgram.isEmpty()) return;
      SegmentChoice rhythmChoice = fabricator.add(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(fabricator.getSegment().getId())
        .setProgramType(Program.Type.Rhythm)
        .setInstrumentType(Instrument.Type.Percussive)
        .setProgramId(rhythmProgram.get().getId())
        .build());

      // rhythm sequence is selected at random of the current program
      // FUTURE: [#166855956] Rhythm Program with multiple Sequences
      var rhythmSequence = fabricator.getSequence(rhythmChoice);

      // voice arrangements
      if (rhythmSequence.isPresent())
        for (ProgramVoice voice : fabricator.getSourceMaterial().getVoices(rhythmProgram.get()))
          craftArrangementForRhythmVoice(rhythmSequence.get(), rhythmChoice, voice);

      // Finally, update the segment with the crafted content
      fabricator.done();

    } catch (FabricationException e) {
      throw exception(String.format("Failed to do Rhythm-Craft Work because %s", e.getMessage()));

    } catch (Exception e) {
      throw exception("Bad failure", e);
    }
  }

  /**
   compute (and cache) the mainProgram

   @return mainProgram
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "chooseRhythmProgram")
  private Optional<Program> chooseRhythmProgram() throws CraftException {
    Segment.Type type;
    type = fabricator.getType();

    switch (type) {
      case Continue:
        Optional<Program> selectedPreviously = getRhythmProgramSelectedPreviouslyForMainProgram();
        return selectedPreviously.isPresent() ? selectedPreviously : chooseFreshRhythm();

      case Initial:
      case NextMain:
      case NextMacro:
        return chooseFreshRhythm();

      default:
        throw exception(String.format("Cannot get Rhythm-type program for unknown fabricator type=%s", type));
    }
  }

  /**
   Determine if a rhythm program has been previously selected
   in one of the previous segments of the current main program
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @return rhythm program if previously selected, or null if none is found
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "getRhythmProgramSelectedPreviouslyForMainProgram")
  private Optional<Program> getRhythmProgramSelectedPreviouslyForMainProgram() {
    try {
      return fabricator.getChoicesOfPreviousSegments()
        .stream()
        .filter(choice -> Program.Type.Rhythm == choice.getProgramType())
        .flatMap(choice -> fabricator.getSourceMaterial().getProgram(choice.getProgramId()).stream())
        .findFirst();

    } catch (FabricationException e) {
      log.warn(formatLog("Could not get rhythm program selected previously for main program"), e);
      return Optional.empty();
    }
  }

  /**
   Choose a fresh rhythm based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Rhythm-Program must match the `minor` or `major` with the Key of the current Segment.

   @return rhythm-type Program
   @throws CraftException on failure
   <p>
   future: actually choose rhythm program
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "chooseFreshRhythm")
  private Optional<Program> chooseFreshRhythm() throws CraftException {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve programs bound to chain and
    // (3) score each source program based on meme isometry
    try {
      for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Rhythm))
        superEntityScorePicker.add(program, scoreRhythm(program));
    } catch (Exception e) {
      throw exception("retrieve programs bound to chain", e);
    }

    // report
    fabricator.putReport("rhythmChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for rhythm program, given current fabricator
   Score includes matching memes, previous segment to macro program first pattern
   <p>
   Returns ZERO if the program has no memes, in order to fix:
   [#162040109] Artist expects program with no memes will never be selected for chain craft.

   @param program to score
   @return score, including +/- entropy; empty if this program has no memes, and isn't directly bound
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "scoreRhythm")
  private Double scoreRhythm(Program program) throws CraftException {
    try {
      double score = 0;
      Collection<String> memes = fabricator.getSourceMaterial().getMemesAtBeginning(program);
      if (!memes.isEmpty())
        score += fabricator.getMemeIsometryOfSegment().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY);

      // [#174435421] Chain bindings specify Program & Instrument within Library
      if (fabricator.isDirectlyBound(program))
        score += SCORE_DIRECTLY_BOUND;

      // score is above zero, else empty
      return score;

    } catch (Exception e) {
      throw exception("score rhythm", e);
    }
  }


  /**
   craft segment events for one rhythm voice
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @param voice to craft events for
   @throws CraftException on failure
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "craftArrangementForRhythmVoice")
  private void craftArrangementForRhythmVoice(ProgramSequence sequence, SegmentChoice choice, ProgramVoice voice) throws CraftException {
    try {
      Optional<String> instrumentId = fabricator.getPreviousVoiceInstrumentId(voice.getId());
      if (instrumentId.isEmpty()) {
        var instrument = chooseFreshPercussiveInstrument(voice);
        if (instrument.isEmpty()) return;
        instrumentId = Optional.of(instrument.get().getId());
      }

      // if no previous instrument found, choose a fresh one
      SegmentChoiceArrangement arrangement = fabricator.add(SegmentChoiceArrangement.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(choice.getSegmentId())
        .setSegmentChoiceId(choice.getId())
        .setProgramVoiceId(voice.getId())
        .setInstrumentId(instrumentId.get())
        .build());

      craftArrangementForVoiceSection(null, sequence, arrangement, voice, 0, fabricator.getSegment().getTotal());

    } catch (FabricationException e) {
      throw
        exception(String.format("Failed to craft arrangement for rhythm voiceId=%s", voice.getId()), e);
    }
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws CraftException on failure
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "chooseFreshPercussiveInstrument")
  private Optional<Instrument> chooseFreshPercussiveInstrument(ProgramVoice voice) throws CraftException {
    try {
      EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

      // (2) retrieve instruments bound to chain
      Collection<Instrument> sourceInstruments = fabricator.getSourceMaterial().getInstrumentsOfType(Instrument.Type.Percussive);

      // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
      log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

      // (3) score each source instrument based on meme isometry
      for (Instrument instrument : sourceInstruments)
        superEntityScorePicker.add(instrument, scorePercussive(instrument));

      // report
      fabricator.putReport("percussiveChoice", superEntityScorePicker.report());

      // (4) return the top choice
      return superEntityScorePicker.getTop();

    } catch (HubClientException e) {
      reportMissing(Instrument.class, "percussive-type bound to Chain");
      return Optional.empty();
    }
  }

  /**
   Score a candidate for percussive instrument, given current fabricator

   @param instrument to score
   @return score, including +/- entropy
   */
  @Trace(resourceName = "nexus/craft/rhythm", operationName = "scorePercussive")
  private double scorePercussive(Instrument instrument) throws CraftException {
    try {
      double score = Chance.normallyAround(0, SCORE_INSTRUMENT_ENTROPY);

      // Score includes matching memes, previous segment to macro instrument first pattern
      score += SCORE_MATCHED_MEMES *
        fabricator.getMemeIsometryOfSegment().score(
          Entities.namesOf(fabricator.getSourceMaterial().getMemes(instrument)));

      // [#174435421] Chain bindings specify Program & Instrument within Library
      if (fabricator.isDirectlyBound(instrument))
        score += SCORE_DIRECTLY_BOUND;

      return score;

    } catch (Exception e) {
      throw exception("score percussive", e);
    }
  }

}
