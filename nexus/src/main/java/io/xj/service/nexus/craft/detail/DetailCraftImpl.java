// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.detail;

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
import io.xj.SegmentChord;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Chance;
import io.xj.lib.util.ValueException;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.craft.arrangement.ArrangementCraftImpl;
import io.xj.service.nexus.fabricator.EntityScorePicker;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.fabricator.Fabricator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends ArrangementCraftImpl implements DetailCraft {
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_DETAIL_ENTROPY = 0.5;
  private static final double SCORE_DIRECTLY_BOUND = 100;

  @Inject
  public DetailCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  @Override
  @Trace(resourceName = "nexus/craft/detail", operationName = "doWork")
  public void doWork() throws NexusException {
    try {
      // for each unique voicing (instrument) types present in the chord voicings of the current main choice
      var voicingTypes = fabricator.getDistinctChordVoicingTypes();

      for (Instrument.Type voicingType : voicingTypes) {
        // program
        Optional<Program> detailProgram = chooseDetailProgram(voicingType);

        // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
        if (detailProgram.isEmpty()) {
          reportMissing(Program.class, String.format("Detail-type with voicing-type %s", voicingType));
          continue;
        }

        SegmentChoice detailChoice = fabricator.add(SegmentChoice.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(fabricator.getSegment().getId())
          .setProgramType(Program.Type.Detail)
          .setInstrumentType(voicingType)
          .setProgramId(detailProgram.get().getId())
          .build());

        // detail sequence is selected at random of the current program
        // FUTURE: [#166855956] Detail Program with multiple Sequences
        var detailSequence = fabricator.getSequence(detailChoice);

        // voice arrangements
        if (detailSequence.isPresent()) {
          var voices = fabricator.getSourceMaterial().getVoices(detailProgram.get());
          if (voices.isEmpty())
            reportMissing(ProgramVoice.class,
              String.format("in Detail-choice Program[%s]", detailProgram.get().getId()));
          for (ProgramVoice voice : voices)
            craftArrangementForDetailVoice(detailSequence.get(), detailChoice, voice);
        }
      }

      // Finally, update the segment with the crafted content
      fabricator.done();

    } catch (NexusException e) {
      throw exception(String.format("Failed to do Detail-Craft Work because %s", e.getMessage()));

    } catch (Exception e) {
      throw exception("Bad failure", e);
    }
  }

  /**
   Choose a detail program having voice(s) of the given type

   @param voicingType of voicing to choose detail program for
   @return Chosen Detail Program
   */
  @Trace(resourceName = "nexus/craft/detail", operationName = "chooseDetailProgram")
  private Optional<Program> chooseDetailProgram(Instrument.Type voicingType) throws NexusException {
    Segment.Type type;
    type = fabricator.getType();

    switch (type) {
      case Continue:
        Optional<Program> selectedPreviously = getDetailProgramSelectedPreviouslyForSegmentMainProgram(voicingType);
        return selectedPreviously.isPresent() ? selectedPreviously : chooseFreshDetailProgram(voicingType);

      case Initial:
      case NextMain:
      case NextMacro:
        return chooseFreshDetailProgram(voicingType);

      default:
        throw exception(String.format("Cannot get Detail-type program for unknown fabricator type=%s", type));
    }
  }

  /**
   Determine if a detail program has been previously selected
   in one of the previous segments of the current main program
   wherein the current pattern of the selected main program
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program

   @param voicingType to get detail program for
   @return detail program if previously selected, or null if none is found
   */
  @Trace(resourceName = "nexus/craft/detail", operationName = "getDetailProgramSelectedPreviouslyForSegmentMainProgram")
  private Optional<Program> getDetailProgramSelectedPreviouslyForSegmentMainProgram(Instrument.Type voicingType) {
    try {
      return fabricator.getChoicesOfPreviousSegments()
        .stream()
        .filter(choice ->
          Program.Type.Detail == choice.getProgramType()
            && voicingType == choice.getInstrumentType())
        .flatMap(choice -> fabricator.getSourceMaterial().getProgram(choice.getProgramId()).stream())
        .findFirst();

    } catch (NexusException e) {
      reportMissing(Program.class, String.format("detail previously selected for %s-type Instrument and main program because fabrication exception %s", voicingType, e.getMessage()));
      return Optional.empty();
    }
  }


  /**
   craft segment events for one detail voice
   <p>
   [#176468964] Rhythm and Detail choices are kept for an entire Main Program
   <p>
   [#176468993] Detail programs can be made to repeat every chord change

   @param sequence from which to craft events
   @param choice   of program
   @param voice    within program
   @throws NexusException on failure to craft
   */
  @Trace(resourceName = "nexus/craft/detail", operationName = "craftArrangementForDetailVoice")
  private void craftArrangementForDetailVoice(
    ProgramSequence sequence,
    SegmentChoice choice,
    ProgramVoice voice
  ) throws NexusException {
    try {
      Optional<String> instrumentId = fabricator.getPreviousVoiceInstrumentId(voice.getId());

      // if no previous instrument found, choose a fresh one
      var instrument = chooseFreshDetailInstrument(voice);

      // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
      if (instrument.isEmpty()) {
        reportMissing(Instrument.class, String.format("Detail-type like %s", voice.getName()));
        return;
      }

      SegmentChoiceArrangement arrangement = fabricator.add(SegmentChoiceArrangement.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(choice.getSegmentId())
        .setSegmentChoiceId(choice.getId())
        .setProgramVoiceId(voice.getId())
        .setInstrumentId(
          instrumentId.orElseGet(() -> chooseFreshDetailInstrument(voice).orElseThrow().getId()))
        .build());

      var program = fabricator.getProgram(choice);
      if (program.isEmpty()) return;
      var programConfig = fabricator.getProgramConfig(program.get());
      if (programConfig.doPatternRestartOnChord() && 0 < fabricator.getSegmentChords().size())
        craftArrangementForDetailVoicePerEachChord(sequence, arrangement, voice);
      else
        craftArrangementForVoiceSection(null, sequence, arrangement, voice, 0, fabricator.getSegment().getTotal());

    } catch (NexusException | ValueException e) {
      throw
        exception(String.format("Failed to craft arrangement for detail voiceId=%s", voice.getId()), e);
    }
  }

  /**
   Choose a fresh detail based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Detail-Program must match the `minor` or `major` with the Key of the current Segment.

   @param voicingType to choose a fresh detail program for-- meaning the detail program will have this type of voice
   @return detail-type Program
   */
  @Trace(resourceName = "nexus/craft/detail", operationName = "chooseFreshDetailProgram")
  private Optional<Program> chooseFreshDetailProgram(Instrument.Type voicingType) {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // Retrieve programs bound to chain having a voice of the specified type
    Map<String/*ID*/, Program> programMap = fabricator.getSourceMaterial()
      .getProgramsOfType(Program.Type.Detail).stream()
      .collect(Collectors.toMap(Program::getId, program -> program));
    Collection<Program> sourcePrograms = fabricator.getSourceMaterial()
      .getAllProgramVoices().stream()
      .filter(programVoice -> voicingType.equals(programVoice.getType()) &&
        programMap.containsKey(programVoice.getProgramId()))
      .map(ProgramVoice::getProgramId)
      .distinct()
      .map(programMap::get)
      .collect(Collectors.toList());

    // (3) score each source program based on meme isometry
    for (Program program : sourcePrograms) superEntityScorePicker.add(program, scoreDetail(program));

    // report
    fabricator.putReport("detailChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();

  }

  /**
   Choose detail instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice to choose instrument for
   @return detail-type Instrument
   */
  @Trace(resourceName = "nexus/craft/detail", operationName = "chooseFreshDetailInstrument")
  protected Optional<Instrument> chooseFreshDetailInstrument(ProgramVoice voice) {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.getSourceMaterial().getInstrumentsOfType(voice.getType());

    // (3) score each source instrument based on meme isometry
    for (Instrument instrument : sourceInstruments)
      superEntityScorePicker.add(instrument, scoreDetail(instrument));

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
  @Trace(resourceName = "nexus/craft/detail", operationName = "scoreDetail")
  protected double scoreDetail(Instrument instrument) {
    double score = Chance.normallyAround(0, SCORE_INSTRUMENT_ENTROPY);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCHED_MEMES *
      fabricator.getMemeIsometryOfSegment().score(
        Entities.namesOf(fabricator.getSourceMaterial().getMemes(instrument)));

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

   @param program to score
   @return score, including +/- entropy; empty if this program has no memes, and isn't directly bound
   */
  @SuppressWarnings("DuplicatedCode")
  @Trace(resourceName = "nexus/craft/detail", operationName = "scoreDetail")
  private Double scoreDetail(Program program) {
    double score = 0;
    Collection<String> memes = fabricator.getSourceMaterial().getMemesAtBeginning(program);
    if (!memes.isEmpty())
      score += fabricator.getMemeIsometryOfSegment().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_DETAIL_ENTROPY);

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;

    // score is above zero, else empty
    return score;

  }

  /**
   Iterate through all the chords of a sequence and arrange events per each chord
   <p>
   [#176468993] Detail programs can be made to repeat every chord change

   @param sequence    from which to craft events
   @param arrangement of instrument
   @param voice       within program
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/detail", operationName = "craftArrangementForDetailVoicePerEachChord")
  private void craftArrangementForDetailVoicePerEachChord(
    ProgramSequence sequence,
    SegmentChoiceArrangement arrangement,
    ProgramVoice voice
  ) throws NexusException {
    try {
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
        craftArrangementForVoiceSection(section.chord, sequence, arrangement, voice, section.fromPos, section.toPos);

    } catch (NexusException e) {
      throw
        exception(String.format("Failed to craft arrangement for detail voiceId=%s per each chord", voice.getId()), e);
    }
  }

  /**
   Representation of a section of an arrangement, having a chord, beginning position and end position
   */
  static class Section {
    public SegmentChord chord;
    public double fromPos;
    public double toPos;
  }

}
