// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.rhythm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Chance;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
    // program
    Optional<Program> program = chooseRhythmProgram();
    if (program.isEmpty()) return;

    // rhythm sequence is selected at random of the current program
    // FUTURE: [#166855956] Rhythm Program with multiple Sequences
    var sequence = fabricator.getRandomlySelectedSequence(program.get());

    // voice arrangements
    if (sequence.isPresent())
      for (ProgramVoice voice : fabricator.getSourceMaterial().getVoices(program.get())) {
        Optional<String> instrumentId = fabricator.getInstrumentIdChosenForVoiceOfSameMainProgram(voice);

        // if no previous instrument found, choose a fresh one
        var instrument = instrumentId.isPresent() ?
          fabricator.getSourceMaterial().getInstrument(instrumentId.get()) :
          chooseFreshPercussiveInstrument(voice);

        // [#176373977] Should gracefully skip voicing type if unfulfilled by rhythm program
        if (instrument.isEmpty()) {
          reportMissing(Instrument.class, String.format("Rhythm-type like %s", voice.getName()));
          return;
        }

        // build primary choice from new ideas
        var primaryChoice = fabricator.add(SegmentChoice.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setType(SegmentChoice.Type.Primary)
          .setInstrumentId(instrument.get().getId())
          .setProgramId(program.get().getId())
          .setProgramSequenceId(sequence.get().getId())
          .setProgramType(program.get().getType())
          .setInstrumentType(instrument.get().getType())
          .setProgramVoiceId(voice.getId())
          .setSegmentId(fabricator.getSegment().getId())
          .build());

/*

FUTURE: this is on the right track, but for rhythm craft we'll need to pay more attention to the individual arrangements

        // Optionally, use the inertial choice that corresponds to this primary one, instead.
        var inertialChoice = computeInertialChoice(primaryChoice);

        // If there's an inertial choice, use it
        if (inertialChoice.isPresent())
          this.craftArrangements(fabricator.add(inertialChoice.get()));
        else
*/
          this.craftArrangements(primaryChoice);
      }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   compute (and cache) the mainProgram

   @return mainProgram
   */
  private Optional<Program> chooseRhythmProgram() throws NexusException {
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
        throw new NexusException(String.format("Cannot get Rhythm-type program for unknown fabricator type=%s", type));
    }
  }

  /**
   Determine if a rhythm program has been previously selected
   in one of the previous segments of the current main program
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @return rhythm program if previously selected, or null if none is found
   */
  private Optional<Program> getRhythmProgramSelectedPreviouslyForMainProgram() {
    return fabricator.getPreferredProgramIds(Program.Type.Rhythm, Instrument.Type.Percussive)
      .stream()
      .flatMap(choice -> fabricator.getSourceMaterial().getProgram(choice).stream())
      .findFirst();
  }

  /**
   Choose a fresh rhythm based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Rhythm-Program must match the `minor` or `major` with the Key of the current Segment.

   @return rhythm-type Program
   */
  private Optional<Program> chooseFreshRhythm() {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve programs bound to chain and
    // (3) score each source program based on meme isometry
    for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Rhythm))
      superEntityScorePicker.add(program, scoreRhythm(program));

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
  @SuppressWarnings("DuplicatedCode")
  private Double scoreRhythm(Program program) {
    double score = 0;
    Collection<String> memes = fabricator.getSourceMaterial().getMemesAtBeginning(program);
    if (!memes.isEmpty())
      score += fabricator.getMemeIsometryOfSegment().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_RHYTHM);

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;

    // score is above zero, else empty
    return score;
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice to choose instrument for
   @return percussive-type Instrument
   */
  private Optional<Instrument> chooseFreshPercussiveInstrument(ProgramVoice voice) {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.getSourceMaterial().getInstrumentsOfType(Instrument.Type.Percussive);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

    // (3) score each source instrument based on meme isometry
    for (Instrument instrument : sourceInstruments)
      superEntityScorePicker.add(instrument, scorePercussive(instrument));

    // (4) prefer same instrument choices throughout a main program
    // Instrument choice inertia
    // https://www.pivotaltracker.com/story/show/178442889
    // If the previously chosen instruments are for the same main program as the current segment,
    // score them all at 95% inertia (almost definitely will choose again)
    if (Segment.Type.Continue.equals(fabricator.getSegment().getType()))
      fabricator.retrospective().getChoices().stream()
        .filter(candidate ->
          candidate.getInstrumentType().equals(Instrument.Type.Percussive) &&
            fabricator.getSourceMaterial().getProgramVoice(candidate.getProgramVoiceId())
              .stream().map(pv -> Objects.equals(voice.getName(), pv.getName()))
              .findFirst()
              .orElse(false))
        .forEach(choice -> superEntityScorePicker.score(choice.getInstrumentId(), SCORE_MATCHED_MAIN_PROGRAM));

    // report
    fabricator.putReport("percussiveChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for percussive instrument, given current fabricator

   @param instrument to score
   @return score, including +/- entropy
   */
  private double scorePercussive(Instrument instrument) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCHED_MEMES *
      fabricator.getMemeIsometryOfSegment().score(
        Entities.namesOf(fabricator.getSourceMaterial().getMemes(instrument)));

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(instrument))
      score += SCORE_DIRECTLY_BOUND;

    return score;
  }

}
