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
import io.xj.nexus.fabricator.MemeIsometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceOfSameMainProgram(Program.Type.Rhythm);

    // Program is from prior choice, or freshly chosen
    Optional<Program> program = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) :
      chooseFreshRhythmProgram();

    // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
    if (program.isEmpty()) {
      reportMissing(Program.class, "Rhythm-type program");
      return;
    }

    // add memes of program to segment in order to affect further choice
    fabricator.addMemes(program.get());

    // [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program
    precomputeRhythmDeltas(program.get().getId());

    // rhythm sequence is selected at random of the current program
    // FUTURE: [#166855956] Rhythm Program with multiple Sequences
    var sequence = fabricator.getRandomlySelectedSequence(program.get());

    // voice arrangements
    if (sequence.isPresent()) {
      var voices = fabricator.sourceMaterial().getVoices(program.get());
      if (voices.isEmpty())
        reportMissing(ProgramVoice.class,
          String.format("in Rhythm-choice Program[%s]", program.get().getId()));

      craftChoices(sequence.get(), voices, this::chooseFreshPercussiveInstrument);
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
    for (Program program : fabricator.sourceMaterial().getProgramsOfType(Program.Type.Rhythm))
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
    double score = 0;
    Collection<String> memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
    if (!memes.isEmpty())
      score += rhythmIsometry.score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_RHYTHM);

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
  private Optional<Instrument> chooseFreshPercussiveInstrument(ProgramVoice voice) throws NexusException {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.sourceMaterial().getInstrumentsOfType(Instrument.Type.Percussive);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

    // (3) score each source instrument based on meme isometry
    MemeIsometry percussiveIsometry = fabricator.getMemeIsometryOfSegment();
    for (Instrument instrument : sourceInstruments)
      superEntityScorePicker.add(instrument, scorePercussive(instrument, percussiveIsometry));

    // (4) prefer same instrument choices throughout a main program
    // Instrument choice inertia
    // https://www.pivotaltracker.com/story/show/178442889
    // If the previously chosen instruments are for the same main program as the current segment,
    // score them all at 95% inertia (almost definitely will choose again)
    if (Segment.Type.Continue.equals(fabricator.getType()))
      fabricator.retrospective().getChoices().stream()
        .filter(candidate ->
          candidate.getInstrumentType().equals(Instrument.Type.Percussive) &&
            fabricator.sourceMaterial().getProgramVoice(candidate.getProgramVoiceId())
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

   @param instrument         to score
   @param percussiveIsometry from which to score percussive instruments
   @return score, including +/- entropy
   */
  private double scorePercussive(Instrument instrument, MemeIsometry percussiveIsometry) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCHED_MEMES *
      percussiveIsometry.score(Entities.namesOf(fabricator.sourceMaterial().getMemes(instrument)));

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(instrument))
      score += SCORE_DIRECTLY_BOUND;

    return score;
  }

}
