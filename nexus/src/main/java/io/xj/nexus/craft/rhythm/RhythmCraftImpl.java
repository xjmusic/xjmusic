// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.rhythm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Instrument;
import io.xj.api.InstrumentState;
import io.xj.api.InstrumentType;
import io.xj.api.Program;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.SegmentChoice;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Chance;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Rhythm craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 [#176625174] RhythmCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class RhythmCraftImpl extends DetailCraftImpl implements RhythmCraft {
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);

  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(ProgramType.RHYTHM);

    // Program is from prior choice, or freshly chosen
    Optional<Program> program = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) :
      chooseFreshRhythmProgram();

    // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
    if (program.isEmpty()) {
      reportMissingInstrumentAudio(Program.class, "Rhythm-type program");
      return;
    }

    // add memes of program to segment in order to affect further choice
    fabricator.addMemes(program.get());

    // [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) ->
      fabricator.sourceMaterial().getProgramVoice(choice.getProgramVoiceId())
        .map(ProgramVoice::getName)
        .orElse("Unknown");
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.RHYTHM.equals(choice.getProgramType());
    var programNames = fabricator.sourceMaterial().getVoices(program.get()).stream()
      .map(ProgramVoice::getName)
      .collect(Collectors.toList());
    precomputeDeltas(choiceFilter, choiceIndexProvider, programNames, fabricator.getTemplateConfig().getDeltaArcRhythmPlateauRatio());

    // rhythm sequence is selected at random of the current program
    // FUTURE: [#166855956] Rhythm Program with multiple Sequences
    var sequence = fabricator.getRandomlySelectedSequence(program.get());

    // voice arrangements
    if (sequence.isPresent()) {
      var voices = fabricator.sourceMaterial().getVoices(program.get());
      if (voices.isEmpty())
        reportMissingInstrumentAudio(ProgramVoice.class,
          String.format("in Rhythm-choice Program[%s]", program.get().getId()));

      craftChoices(sequence.get(), voices, this::chooseFreshPercussiveInstrument, true);
    }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Choose a fresh rhythm based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Rhythm-Program must match the `minor` or `major` with the Key of the current Segment.

   @return rhythm-type Program
   */
  private Optional<Program> chooseFreshRhythmProgram() {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve programs bound to chain and
    // (3) score each source program based on meme isometry
    MemeIsometry rhythmIsometry = fabricator.getMemeIsometryOfSegment();
    for (Program program : fabricator.sourceMaterial().getProgramsOfType(ProgramType.RHYTHM))
      superEntityScorePicker.add(program, scoreRhythm(program, rhythmIsometry));

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

   @param program        to score
   @param rhythmIsometry from which to score programs
   @return score, including +/- entropy; empty if this program has no memes, and isn't directly bound
   */
  @SuppressWarnings("DuplicatedCode")
  private Double scoreRhythm(Program program, MemeIsometry rhythmIsometry) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_RHYTHM);
    Collection<String> memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
    if (!memes.isEmpty())
      score += rhythmIsometry.score(memes) * SCORE_MATCH_MEMES;

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;
    else if (Objects.equals(program.getState(), ProgramState.DRAFT))
      score += SCORE_UNPUBLISHED;

    // score is above zero, else empty
    return score;
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice to choose instrument for
   @return drum-type Instrument
   */
  private Optional<Instrument> chooseFreshPercussiveInstrument(ProgramVoice voice) throws NexusException {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.sourceMaterial().getInstrumentsOfType(InstrumentType.DRUM);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

    // (3) score each source instrument based on meme isometry
    MemeIsometry percussiveIsometry = fabricator.getMemeIsometryOfSegment();
    for (Instrument instrument : sourceInstruments)
      superEntityScorePicker.add(instrument, scorePercussive(instrument, percussiveIsometry));

    switch (fabricator.getType()) {
      case CONTINUE ->
        // Instrument choice inertia: prefer same instrument choices throughout a main program
        // https://www.pivotaltracker.com/story/show/178442889
        fabricator.retrospective().getChoices().stream()
          .filter(candidate -> Objects.equals(candidate.getInstrumentType(), voice.getType())
            && fabricator.sourceMaterial().getProgramVoice(candidate.getProgramVoiceId())
            .stream().map(pv -> Objects.equals(voice.getName(), pv.getName()))
            .findFirst()
            .orElse(false))
          .forEach(choice -> superEntityScorePicker.score(choice.getInstrumentId(), SCORE_MATCH_MAIN_PROGRAM));

      case NEXTMAIN, NEXTMACRO -> // Keep same instruments when carrying outgoing choices to incoming choices of next segment
        // https://www.pivotaltracker.com/story/show/179126302
        fabricator.retrospective().getPreviousChoiceOfVoice(voice.getId())
          .ifPresent(ch -> {
            if (isUnlimitedIn(ch))
              fabricator.retrospective().getChoices().stream()
                .filter(candidate -> Objects.equals(candidate.getInstrumentType(), voice.getType()))
                .filter(this::isUnlimitedOut)
                .forEach(choice -> superEntityScorePicker.score(choice.getInstrumentId(), SCORE_MATCH_OUTGOING_TO_INCOMING));
          });
    }


    // report
    fabricator.putReport("percussiveChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for drum instrument, given current fabricator

   @param instrument         to score
   @param percussiveIsometry from which to score drum instruments
   @return score, including +/- entropy
   */
  private double scorePercussive(Instrument instrument, MemeIsometry percussiveIsometry) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCH_MEMES *
      percussiveIsometry.score(Entities.namesOf(fabricator.sourceMaterial().getMemes(instrument)));

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(instrument))
      score += SCORE_DIRECTLY_BOUND;
    else if (Objects.equals(instrument.getState(), InstrumentState.DRAFT))
      score += SCORE_UNPUBLISHED;

    return score;
  }

}
