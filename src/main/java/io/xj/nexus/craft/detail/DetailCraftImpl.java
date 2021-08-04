// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.detail;

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
import io.xj.nexus.craft.arrangement.ArrangementCraftImpl;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends ArrangementCraftImpl implements DetailCraft {

  @Inject
  public DetailCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    // [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program
    precomputeDetailDeltas();

    for (Instrument.Type voicingType : fabricator.getDistinctChordVoicingTypes()) {
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceOfSameMainProgram(voicingType);

      // Program is from prior choice, or freshly chosen
      Optional<Program> program = priorChoice.isPresent() ?
        fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) :
        chooseFreshDetailProgram(voicingType);

      // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
      if (program.isEmpty()) {
        reportMissing(Program.class, String.format("Detail-type with voicing-type %s", voicingType));
        continue;
      }

      // add memes of program to segment in order to affect further choice
      fabricator.addMemes(program.get());

      // detail sequence is selected at random of the current program
      // FUTURE: [#166855956] Detail Program with multiple Sequences
      var sequence = fabricator.getRandomlySelectedSequence(program.get());

      // voice arrangements
      if (sequence.isPresent()) {
        var voices = fabricator.sourceMaterial().getVoices(program.get());
        if (voices.isEmpty())
          reportMissing(ProgramVoice.class,
            String.format("in Detail-choice Program[%s]", program.get().getId()));
        craftChoices(sequence.get(), voices, this::chooseFreshDetailInstrument);
      }
    }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Choose a fresh detail based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Detail-Program must match the `minor` or `major` with the Key of the current Segment.

   @param voicingType to choose a fresh detail program for-- meaning the detail program will have this type of voice
   @return detail-type Program
   */
  private Optional<Program> chooseFreshDetailProgram(Instrument.Type voicingType) {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // Retrieve programs bound to chain having a voice of the specified type
    Map<String/*ID*/, Program> programMap = fabricator.sourceMaterial()
      .getProgramsOfType(Program.Type.Detail).stream()
      .collect(Collectors.toMap(Program::getId, program -> program));
    Collection<Program> sourcePrograms = fabricator.sourceMaterial()
      .getAllProgramVoices().stream()
      .filter(programVoice -> voicingType.equals(programVoice.getType()) &&
        programMap.containsKey(programVoice.getProgramId()))
      .map(ProgramVoice::getProgramId)
      .distinct()
      .map(programMap::get)
      .collect(Collectors.toList());

    // (3) score each source program based on meme isometry
    MemeIsometry detailIsometry = fabricator.getMemeIsometryOfSegment();
    for (Program program : sourcePrograms) superEntityScorePicker.add(program, scoreDetail(program, detailIsometry));

    // report
    fabricator.putReport("detailChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();

  }

  /**
   Choose detail instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice of instrument to choose
   @return detail-type Instrument
   */
  protected Optional<Instrument> chooseFreshDetailInstrument(ProgramVoice voice) throws NexusException {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.sourceMaterial().getInstrumentsOfType(voice.getType());

    // (3) score each source instrument based on meme isometry
    for (Instrument instrument : sourceInstruments)
      if (instrument.getType().equals(voice.getType()))
        superEntityScorePicker.add(instrument, scoreDetail(instrument));

    // (4) prefer same instrument choices throughout a main program
    // Instrument choice inertia
    // https://www.pivotaltracker.com/story/show/178442889
    // If the previously chosen instruments are for the same main program as the current segment,
    // score them all at 95% inertia (almost definitely will choose again)
    if (Segment.Type.Continue.equals(fabricator.getType()))
      fabricator.retrospective().getChoices().stream()
        .filter(candidate -> candidate.getInstrumentType().equals(voice.getType()))
        .forEach(choice -> superEntityScorePicker.score(choice.getInstrumentId(), SCORE_MATCHED_MAIN_PROGRAM));

    // report
    fabricator.putReport("detailChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for detail instrument, given current fabricator

   @param instrument to score
   @return score, including +/- entropy
   */
  protected double scoreDetail(Instrument instrument) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCHED_MEMES *
      fabricator.getMemeIsometryOfSegment().score(
        Entities.namesOf(fabricator.sourceMaterial().getMemes(instrument)));

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(instrument))
      score += SCORE_DIRECTLY_BOUND;

    return score;
  }

  /**
   Score a candidate for detail program, given current fabricator
   Score includes matching memes, previous segment to macro program first pattern
   <p>
   Returns ZERO if the program has no memes, in order to fix:
   [#162040109] Artist expects program with no memes will never be selected for chain craft.

   @param program        to score
   @param detailIsometry from which to score detail programs
   @return score, including +/- entropy; empty if this program has no memes, and isn't directly bound
   */
  @SuppressWarnings("DuplicatedCode")
  private Double scoreDetail(Program program, MemeIsometry detailIsometry) {
    double score = 0;
    Collection<String> memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
    if (!memes.isEmpty())
      score += detailIsometry.score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_DETAIL);

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;

    // score is above zero, else empty
    return score;

  }

}
