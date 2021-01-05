// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.detail;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.*;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.music.Key;
import io.xj.lib.music.Note;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.service.hub.client.HubClientException;
import io.xj.service.nexus.craft.CraftImpl;
import io.xj.service.nexus.craft.exception.CraftException;
import io.xj.service.nexus.fabricator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends CraftImpl implements DetailCraft {
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_DETAIL_ENTROPY = 0.5;
  private static final double SCORE_DIRECTLY_BOUND = 100;
  private static final String KEY_VOICE_NAME_TEMPLATE = "%s_%s";
  private final Logger log = LoggerFactory.getLogger(DetailCraftImpl.class);
  private final SecureRandom random = new SecureRandom();

  @Inject
  public DetailCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  @Override
  public void doWork() throws CraftException {
    Map<String, InstrumentAudio> previousInstrumentAudio = getPreviousInstrumentAudio();
    try {
      // for each unique voicing (instrument) types present in the chord voicings of the current main choice
      var voicingTypes = fabricator.getDistinctChordVoicingTypes();
      if (voicingTypes.isEmpty())
        log.info("Found no chord voicing types in Main-choice Program[{}]",
          fabricator.getCurrentMainChoice().getId());
      else
        for (Instrument.Type voicingType : voicingTypes) {
          // program
          Program detailProgram = chooseDetailProgram(voicingType);
          SegmentChoice detailChoice = fabricator.add(SegmentChoice.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSegmentId(fabricator.getSegment().getId())
            .setProgramType(Program.Type.Detail)
            .setProgramId(detailProgram.getId())
            .setTranspose(computeDetailTranspose(detailProgram))
            .build());

          // detail sequence is selected at random of the current program
          // FUTURE: [#166855956] Detail Program with multiple Sequences
          var detailSequence = fabricator.getSequence(detailChoice);

          // voice arrangements
          var voices = fabricator.getSourceMaterial().getVoices(detailProgram);
          if (voices.isEmpty())
            log.info("Found no voices in Detail-choice Program[{}]",
              detailProgram.getId());
          for (ProgramVoice voice : voices)
            craftArrangementForDetailVoice(detailSequence, detailChoice, voice, previousInstrumentAudio);
        }

      // Finally, update the segment with the crafted content
      fabricator.done();

    } catch (FabricationException e) {
      throw exception(String.format("Failed to do Detail-Craft Work because %s", e.getMessage()));

    } catch (Exception e) {
      throw exception("Bad failure", e);
    }
  }

  /**
   Key for any pick designed to collide at same voice id + name

   @param pick to get key of
   @return unique key for arrangement pick
   */
  private String eventKey(SegmentChoiceArrangementPick pick) throws CraftException {
    try {
      return String.format(KEY_VOICE_NAME_TEMPLATE, fabricator.getSourceMaterial().getVoice(fabricator.getSourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId())).getId(), pick.getName());

    } catch (HubClientException e) {
      throw exception("unique key for arrangement pick", e);
    }
  }

  /**
   Key for any pattern event designed to collide at same voice id + name

   @param event to get key of
   @return unique key for pattern event
   */
  private String eventKey(ProgramSequencePatternEvent event) throws CraftException {
    try {
      return String.format(KEY_VOICE_NAME_TEMPLATE, fabricator.getSourceMaterial().getVoice(event).getId(), getTrackName(event));

    } catch (HubClientException e) {
      throw exception("unique key for pattern event", e);
    }
  }

  /**
   Get the track name for a give event

   @param event to get voice String of
   @return Track name
   */
  private String getTrackName(ProgramSequencePatternEvent event) throws CraftException {
    try {
      return fabricator.getSourceMaterial().getTrack(event).getName();

    } catch (HubClientException e) {
      throw exception("track name for event event", e);
    }
  }

  /**
   Get previously chosen (for previous segments with same main program and meme constellation) instrument audio

   @return map of previous chosen instrument audio
   @throws CraftException on failure to build map
   */
  private Map<String, InstrumentAudio> getPreviousInstrumentAudio() throws CraftException {
    Map<String, InstrumentAudio> previousInstrumentAudio = Maps.newHashMap();
    try {
      String con = fabricator.getMemeIsometryOfSegment().getConstellation();
      if (fabricator.getMemeConstellationPicksOfPreviousSegments().containsKey(con)) {
        Collection<SegmentChoiceArrangementPick> picks = fabricator.getMemeConstellationPicksOfPreviousSegments().get(con);
        log.info("[segId={}] previous meme constellation picks {}", fabricator.getSegment().getId(), picks.size());
        for (SegmentChoiceArrangementPick pick : picks) {
          String key = eventKey(pick);
          previousInstrumentAudio.put(key, fabricator.getSourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId()));
        }
      }
      return previousInstrumentAudio;

    } catch (FabricationException | HubClientException e) {
      throw exception("Unable to build map create previous instrument audio", e);
    }
  }

  /**
   Choose a detail program having voice(s) of the given type

   @param voicingType of voicing to choose detail program for
   @return Chosen Detail Program
   */
  private Program chooseDetailProgram(Instrument.Type voicingType) throws CraftException {
    Segment.Type type;
    try {
      type = fabricator.getType();
    } catch (FabricationException e) {
      throw exception("Cannot get fabricator type, in order to choose detail program", e);
    }

    switch (type) {
      case Continue:
        Optional<Program> selectedPreviously = getDetailProgramSelectedPreviouslyForSegmentMemeConstellation(voicingType);
        return selectedPreviously.isPresent() ? selectedPreviously.get() : chooseFreshDetail(voicingType);

      case Initial:
      case NextMain:
      case NextMacro:
        return chooseFreshDetail(voicingType);

      default:
        throw exception(String.format("Cannot get Detail-type program for unknown fabricator type=%s", type));
    }
  }

  /**
   Determine if a detail program has been previously selected
   in one of the previous segments of the current main program
   wherein the current pattern of the selected main program
   has a non-unique (previously encountered) meme constellation
   <p>
   Compute the pattern-meme constellations of any previous segments which selected the same main program
   <p>
   [#161736024] for each unique program-pattern-meme constellation within the main program

   @param voicingType to get detail program for
   @return detail program if previously selected, or null if none is found
   */
  private Optional<Program> getDetailProgramSelectedPreviouslyForSegmentMemeConstellation(Instrument.Type voicingType) {
    try {
      var constellationProgramIds = Maps.<String, String>newHashMap();
      String con = fabricator.getMemeIsometryOfSegment().getConstellation();
      if (fabricator.getMemeConstellationChoicesOfPreviousSegments().containsKey(con))
        for (SegmentChoice choice : fabricator.getMemeConstellationChoicesOfPreviousSegments().get(con))
          if (Program.Type.Detail == choice.getProgramType() && voicingType == choice.getInstrumentType())
            constellationProgramIds.put(con, fabricator.getProgram(choice).getId());

      String constellation = MemeIsometry.ofMemes(Entities.namesOf(fabricator.getSegmentMemes())).getConstellation();
      return constellationProgramIds.containsKey(constellation) ? Optional.of(fabricator.getSourceMaterial().getProgram(constellationProgramIds.get(constellation))) : Optional.empty();

    } catch (FabricationException | HubClientException | EntityException e) {
      log.warn(formatLog("Could not get detail program selected previously for segment meme constellation"), e);
      return Optional.empty();
    }
  }

  /**
   Determine if an arrangement has been previously crafted
   in one of the previous segments of the current main sequence
   wherein the current pattern of the selected main sequence
   has a non-unique (previously encountered) meme constellation
   and a voice we have encountered for that meme constellation
   <p>
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] for each unique sequence-pattern-meme constellation within the main sequence

   @return detail sequence if previously selected, or null if none is found
   */
  private Optional<String> getPreviousVoiceInstrumentId(String segmentMemeConstellation, String voiceId) {
    try {
      for (String constellation : fabricator.getMemeConstellationArrangementsOfPreviousSegments().keySet()) {
        if (Objects.equals(segmentMemeConstellation, constellation))
          for (SegmentChoiceArrangement arrangement : fabricator.getMemeConstellationArrangementsOfPreviousSegments().get(constellation)) {
            if (Objects.equals(voiceId, arrangement.getProgramVoiceId()))
              return Optional.of(arrangement.getInstrumentId());
          }
      }

    } catch (FabricationException e) {
      log.warn(formatLog(String.format("Could not get previous voice instrumentId for segmentMemeConstellation=%s, voiceId=%s", segmentMemeConstellation, voiceId)), e);
    }
    return Optional.empty();
  }

  /**
   Transposition for detail-type sequence choice for segment

   @param detailProgram to get transpose of
   @return +/- semitones transposition of detail-type sequence choice
   */
  private Integer computeDetailTranspose(Program detailProgram) {
    return Key.delta(detailProgram.getKey(), fabricator.getSegment().getKey(), 0);
  }


  /**
   Choose a fresh detail based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Detail-Program must match the `minor` or `major` with the Key of the current Segment.

   @param voicingType to choose a fresh detail program for-- meaning the detail program will have this type of voice
   @return detail-type Program
   @throws CraftException on failure
   <p>
   future: actually choose detail program
   */
  private Program chooseFreshDetail(Instrument.Type voicingType) throws CraftException {
    try {
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

    } catch (FabricationException | HubClientException e) {
      throw exception(String.format(
        "Found no detail-type program with %s-type voice bound to Chain!", voicingType), e);
    }
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
  private Double scoreDetail(Program program) throws CraftException {
    try {
      double score = 0;
      Collection<String> memes = fabricator.getSourceMaterial().getMemesAtBeginning(program);
      if (!memes.isEmpty())
        score += fabricator.getMemeIsometryOfSegment().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_DETAIL_ENTROPY);

      // [#174435421] Chain bindings specify Program & Instrument within Library
      if (fabricator.isDirectlyBound(program))
        score += SCORE_DIRECTLY_BOUND;

      // score is above zero, else empty
      return score;

    } catch (HubClientException | EntityException e) {
      throw exception("score detail program", e);
    }
  }


  /**
   craft segment events for one detail voice
   [#161736024] if segment meme constellation already encountered, use that instrument-voice

   @param voice to craft events for
   @throws CraftException on failure
   */
  private void craftArrangementForDetailVoice(ProgramSequence sequence, SegmentChoice choice, ProgramVoice voice, Map<String, InstrumentAudio> previousInstrumentAudio) throws CraftException {
    try {
      String constellation = MemeIsometry.ofMemes(Entities.namesOf(fabricator.getSegmentMemes())).getConstellation();
      Optional<String> instrumentId = getPreviousVoiceInstrumentId(constellation, voice.getId());

      // if no previous instrument found, choose a fresh one
      SegmentChoiceArrangement arrangement = fabricator.add(SegmentChoiceArrangement.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(choice.getSegmentId())
        .setSegmentChoiceId(choice.getId())
        .setProgramVoiceId(voice.getId())
        .setInstrumentId(
          instrumentId.isPresent() ?
            instrumentId.get() : chooseFreshDetailInstrument(voice).getId())
        .build());

      // choose intro pattern (if available)
      Optional<ProgramSequencePattern> introPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePattern.Type.Intro);

      // choose outro pattern (if available)
      Optional<ProgramSequencePattern> outroPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePattern.Type.Outro);

      // compute in and out points, and length # beats for which loop patterns will be required
      long loopOutPos = fabricator.getSegment().getTotal() - (outroPattern.map(ProgramSequencePattern::getTotal).orElse(0));

      // begin at the beginning and fabricate events for the segment of beginning to end
      double curPos = 0.0;

      // if intro pattern, fabricate those voice event first
      if (introPattern.isPresent())
        curPos += craftDetailPatternEvents(previousInstrumentAudio, choice, arrangement, introPattern.get(), curPos, loopOutPos, 0);

      // choose loop patterns until arrive at the out point or end of segment
      while (curPos < loopOutPos) {
        Optional<ProgramSequencePattern> loopPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePattern.Type.Loop);
        if (loopPattern.isPresent())
          curPos += craftDetailPatternEvents(previousInstrumentAudio, choice, arrangement, loopPattern.get(), curPos, loopOutPos, 0);
        else
          curPos = loopOutPos;
      }

      // "Go for it" more towards the end of a program (and only during the outro, when present)
      double goForItRatio = fabricator.getSequenceBindingOffsetForChoice(fabricator.getCurrentMainChoice()).doubleValue() /
        fabricator.getMaxAvailableSequenceBindingOffset(fabricator.getCurrentMainChoice()).doubleValue();

      // if outro pattern, fabricate those voice event last
      // [#161466708] compute how much to go for it in the outro
      if (outroPattern.isPresent())
        craftDetailPatternEvents(previousInstrumentAudio, choice, arrangement, outroPattern.get(), curPos, loopOutPos, goForItRatio);

    } catch (FabricationException | EntityException e) {
      throw
        exception(String.format("Failed to craft arrangement for detail voiceId=%s", voice.getId()), e);
    }
  }

  /**
   Choose detail instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice to choose instrument for
   @return detail-type Instrument
   @throws CraftException on failure
   */
  private Instrument chooseFreshDetailInstrument(ProgramVoice voice) throws CraftException {
    try {
      EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

      // (2) retrieve instruments bound to chain
      Collection<Instrument> sourceInstruments = fabricator.getSourceMaterial().getInstrumentsOfType(voice.getType());

      // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
      log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

      // (3) score each source instrument based on meme isometry
      for (Instrument instrument : sourceInstruments)
        superEntityScorePicker.add(instrument, scoreDetail(instrument));

      // report
      fabricator.putReport("detailChoice", superEntityScorePicker.report());

      // (4) return the top choice
      return superEntityScorePicker.getTop();

    } catch (FabricationException | HubClientException e) {
      throw exception("Found no detail-type instrument bound to Chain!", e);
    }
  }

  /**
   Score a candidate for detail instrument, given current fabricator

   @param instrument to score
   @return score, including +/- entropy
   */
  private double scoreDetail(Instrument instrument) throws CraftException {
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

    } catch (HubClientException | EntityException e) {
      throw exception("score detail program", e);
    }
  }

  /**
   Craft the voice events of a single detail pattern.
   [#161601279] Artist during detail craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

   @param previousInstrumentAudio map of previous instrument audio of which to potentially select
   @param choice                  to craft pattern events for
   @param arrangement             to craft pattern events for
   @param pattern                 to source events
   @param fromPos                 to write events to segment
   @param maxPos                  to write events to segment
   @param goForItRatio            entropy is increased during the progression of a main sequence [#161466708]
   @return deltaPos of start, after crafting this batch of detail pattern events
   */
  private double craftDetailPatternEvents(Map<String, InstrumentAudio> previousInstrumentAudio, SegmentChoice choice, SegmentChoiceArrangement arrangement, ProgramSequencePattern pattern, double fromPos, double maxPos, double goForItRatio) throws CraftException {
    try {
      if (Objects.isNull(pattern)) throw exception("Cannot craft create null pattern");
      double totalPos = maxPos - fromPos;
      Collection<ProgramSequencePatternEvent> events = fabricator.getSourceMaterial().getEvents(pattern);
      Instrument instrument = fabricator.getSourceMaterial().getInstrument(arrangement.getInstrumentId());
      for (ProgramSequencePatternEvent event : events) {
        double chanceOfRandomChoice = 0.0 == goForItRatio ? 0.0 : goForItRatio * Value.ratio(event.getPosition() - fromPos, totalPos);
        pickInstrumentAudio(previousInstrumentAudio, instrument, arrangement, event, choice.getTranspose(), fromPos, chanceOfRandomChoice);
      }
      return Math.min(totalPos, pattern.getTotal());

    } catch (HubClientException e) {
      throw exception("craft detail pattern events", e);
    }
  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param previousInstrumentAudio map of previous instrument audio of which to potentially select
   @param event                   to pick audio for
   @param shiftPosition           offset voice event zero within current segment
   @param chanceOfRandomChoice    entropy is increased during the progression of a main sequence [#161466708]
   */
  private void pickInstrumentAudio(Map<String, InstrumentAudio> previousInstrumentAudio, Instrument instrument, SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent event, int transpose, Double shiftPosition, Double chanceOfRandomChoice) throws CraftException {
    try {
      var audio = selectInstrumentAudio(previousInstrumentAudio, instrument, event, chanceOfRandomChoice);

      // Morph & Point attributes are expressed in beats
      double position = event.getPosition() + shiftPosition;
      double duration = event.getDuration();
      SegmentChord chord = fabricator.getChordAt((int) Math.floor(position))
        .orElseThrow(() -> new FabricationException("No Segment Chord found!"));
      SegmentChordVoicing voicing = fabricator.getVoicing(chord, instrument.getType())
        .orElseThrow(() -> new FabricationException(String.format("No %s Segment Chord Voicing found!",
          instrument.getType())));

      // The final note is voiced
      Note note = DetailCraftVoiceNotePicker.from(
        fabricator.getKeyForArrangement(segmentChoiceArrangement),
        Note.of(event.getNote()).transpose(transpose),
        chord, voicing, audio, fabricator.getTuning()).pick();

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.computeSecondsAtPosition(position);
      double lengthSeconds = fabricator.computeSecondsAtPosition(position + duration) - startSeconds;

      // of pick
      fabricator.add(SegmentChoiceArrangementPick.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segmentChoiceArrangement.getSegmentId())
        .setSegmentChoiceArrangementId(segmentChoiceArrangement.getId())
        .setInstrumentAudioId(audio.getId())
        .setProgramSequencePatternEventId(event.getId())
        .setName(getTrackName(event))
        .setStart(startSeconds)
        .setLength(lengthSeconds)
        .setAmplitude(event.getVelocity())
        .setPitch(fabricator.getPitch(note))
        .build());

    } catch (FabricationException e) {
      throw exception(String.format("Could not pick audio for instrumentId=%s, arrangementId=%s, eventId=%s, transpose=%d, shiftPosition=%f, chanceOfRandomChoice=%f",
        instrument.getId(), segmentChoiceArrangement.getId(), event.getId(), transpose, shiftPosition, chanceOfRandomChoice), e);
    }
  }

  /**
   Determine if we will use a cached or new audio for this selection
   Cached audio defaults to random selection if none has been previously encountered

   @param previousInstrumentAudio map of previous instrument audio of which to potentially select
   @param instrument              of which to score available audios, and make a selection
   @param event                   to match
   @param chanceOfRandomChoice    of 0 to 1, chance that a random audio will be selected (instead of the cached selection)
   @return matched new audio
   @throws CraftException on failure
   */
  private InstrumentAudio selectInstrumentAudio(Map<String, InstrumentAudio> previousInstrumentAudio, Instrument instrument, ProgramSequencePatternEvent event, Double chanceOfRandomChoice) throws CraftException {
    if (0 < chanceOfRandomChoice && random.nextDouble() <= chanceOfRandomChoice) {
      return selectNewInstrumentAudio(instrument, event);
    } else {
      return selectPreviousInstrumentAudio(previousInstrumentAudio, instrument, event);
    }
  }

  /**
   Select the cached (already selected for this segment+drum name)
   instrument audio based on a pattern event.
   <p>
   If never encountered, default to new selection and cache that.

   @param previousInstrumentAudio map of previous instrument audio of which to potentially select
   @param instrument              of which to score available audios, and make a selection
   @param event                   to match
   @return matched new audio
   @throws CraftException on failure
   */
  private InstrumentAudio selectPreviousInstrumentAudio(Map<String, InstrumentAudio> previousInstrumentAudio, Instrument instrument, ProgramSequencePatternEvent event) throws CraftException {
    String key = eventKey(event);
    if (!previousInstrumentAudio.containsKey(key))
      previousInstrumentAudio.put(key, selectNewInstrumentAudio(instrument, event));
    return previousInstrumentAudio.get(key);
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   @return matched new audio
   @throws CraftException on failure
   */
  private InstrumentAudio selectNewInstrumentAudio(Instrument instrument, ProgramSequencePatternEvent event) throws CraftException {
    try {
      EntityScorePicker<InstrumentAudio> audioEntityScorePicker = new EntityScorePicker<>();

      // add all audio to chooser
      audioEntityScorePicker.addAll(fabricator.getSourceMaterial().getAudios(instrument));

      // score each audio against the current voice event, with some variability
      for (InstrumentAudioEvent audioEvent : fabricator.getSourceMaterial().getFirstEventsOfAudiosOfInstrument(instrument))
        audioEntityScorePicker.score(audioEvent.getInstrumentAudioId(),
          Chance.normallyAround(
            NameIsometry.similarity(getTrackName(event), audioEvent.getName()),
            SCORE_INSTRUMENT_ENTROPY));

      // final chosen audio event
      return audioEntityScorePicker.getTop();

    } catch (FabricationException | HubClientException e) {
      throw exception(String.format("No acceptable Audio found for instrumentId=%s, eventId=%s", instrument.getId(), event.getId()), e);
    }
  }
}
