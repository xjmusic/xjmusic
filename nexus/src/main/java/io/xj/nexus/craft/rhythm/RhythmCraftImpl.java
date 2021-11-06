// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.rhythm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
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
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(ProgramType.Rhythm);

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
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Rhythm.toString().equals(choice.getProgramType());
    var programNames = fabricator.sourceMaterial().getVoices(program.get()).stream()
      .map(ProgramVoice::getName)
      .collect(Collectors.toList());
    precomputeDeltas(choiceFilter, choiceIndexProvider, programNames,
      fabricator.getTemplateConfig().getDeltaArcRhythmPlateauRatio(),
      fabricator.getTemplateConfig().getDeltaArcRhythmPlateauShiftRatio(),
      fabricator.getTemplateConfig().getDeltaArcRhythmLayersIncoming(),
      fabricator.getTemplateConfig().getDeltaArcRhythmLayersOutgoing());

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
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;
    for (Program program : fabricator.sourceMaterial().getProgramsOfType(ProgramType.Rhythm)) {
      memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
      if (iso.isAllowed(memes))
        superEntityScorePicker.add(program, scoreRhythm(iso, program, memes));
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

   @param iso     from which to score programs
   @param program to score
   @param memes   to score
   @return score, including +/- entropy; empty if this program has no memes, and isn't directly bound
   */
  @SuppressWarnings("DuplicatedCode")
  private Double scoreRhythm(MemeIsometry iso, Program program, Collection<String> memes) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_RHYTHM);
    if (!memes.isEmpty())
      score += iso.score(memes) * SCORE_MATCH_MEMES;

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;
    else if (Objects.equals(program.getState(), ProgramState.Draft))
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
    Collection<Instrument> sourceInstruments = fabricator.sourceMaterial().getInstrumentsOfType(InstrumentType.Drum);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

    // (3) score each source instrument based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;
    for (Instrument instrument : sourceInstruments) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemes(instrument));
      if (iso.isAllowed(memes))
        superEntityScorePicker.add(instrument, scorePercussive(iso, instrument, memes));
    }

    switch (fabricator.getType()) {
      case CONTINUE ->
        // Instrument choice inertia: prefer same instrument choices throughout a main program
        // https://www.pivotaltracker.com/story/show/178442889
        fabricator.retrospective().getChoices().stream()
          .filter(candidate -> Objects.equals(candidate.getInstrumentType(), voice.getType().toString())
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
                .filter(candidate -> Objects.equals(candidate.getInstrumentType(), voice.getType().toString()))
                .filter(DetailCraftImpl::isUnlimitedOut)
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

   @param iso        from which to score drum instruments
   @param instrument to score
   @param memes      to score
   @return score, including +/- entropy
   */
  protected double scorePercussive(MemeIsometry iso, Instrument instrument, Collection<String> memes) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCH_MEMES * iso.score(memes);

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(instrument))
      score += SCORE_DIRECTLY_BOUND;
    else if (Objects.equals(instrument.getState(), InstrumentState.Draft))
      score += SCORE_UNPUBLISHED;

    return score;
  }

}
