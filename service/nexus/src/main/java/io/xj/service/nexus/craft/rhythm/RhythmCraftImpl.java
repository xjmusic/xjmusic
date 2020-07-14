// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.rhythm;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.MemeEntity;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.music.Note;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.service.hub.entity.*;
import io.xj.service.nexus.craft.CraftImpl;
import io.xj.service.nexus.craft.exception.CraftException;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentType;
import io.xj.service.nexus.fabricator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.*;

/**
 Rhythm craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class RhythmCraftImpl extends CraftImpl implements RhythmCraft {
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private static final String KEY_VOICE_NAME_TEMPLATE = "%s_%s";
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);
  private final SecureRandom random = new SecureRandom();

  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  /**
   Key for any pick designed to collide at same voice id + name

   @param pick to get key of
   @return unique key for pattern event
   */
  private String eventKey(SegmentChoiceArrangementPick pick) {
    return String.format(KEY_VOICE_NAME_TEMPLATE, fabricator.getSourceMaterial().getVoice(fabricator.getSourceMaterial().getProgramEvent(pick.getProgramSequencePatternEventId())).getId(), pick.getName());
  }

  /**
   Key for any pattern event designed to collide at same voice id + name

   @param event to get key of
   @return unique key for pattern event
   */
  private String eventKey(ProgramSequencePatternEvent event) {
    return String.format(KEY_VOICE_NAME_TEMPLATE, fabricator.getSourceMaterial().getVoice(event).getId(), getTrackName(event));
  }

  /**
   Get the Voice ID of a given event

   @param event to get voice UUID of
   @return Track name
   */
  private String getTrackName(ProgramSequencePatternEvent event) {
    return fabricator.getSourceMaterial().getTrack(event).getName();
  }

  @Override
  public void doWork() throws CraftException {
    Map<String, InstrumentAudio> previousInstrumentAudio = getPreviousInstrumentAudio();
    try {

      // program
      Program rhythmProgram = chooseRhythmProgram();
      SegmentChoice rhythmChoice = fabricator.add(SegmentChoice.create(fabricator.getSegment(), ProgramType.Rhythm, rhythmProgram, computeRhythmTranspose(rhythmProgram)));

      // rhythm sequence is selected at random of the current program
      // FUTURE: [#166855956] Rhythm Program with multiple Sequences
      ProgramSequence rhythmSequence = fabricator.getSequence(rhythmChoice);

      // voice arrangements
      for (ProgramVoice voice : fabricator.getSourceMaterial().getVoices(rhythmProgram))
        craftArrangementForRhythmVoice(rhythmSequence, rhythmChoice, voice, previousInstrumentAudio);

      // Finally, update the segment with the crafted content
      fabricator.done();

    } catch (FabricationException e) {
      throw exception("Failed to do Rhythm-Craft Work", e);
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
    } catch (FabricationException e) {
      throw new CraftException("Unable to build map create previous instrument audio", e);
    }
    return previousInstrumentAudio;
  }

  /**
   compute (and cache) the mainProgram

   @return mainProgram
   */
  private Program chooseRhythmProgram() throws CraftException {
    SegmentType type;
    try {
      type = fabricator.getType();
    } catch (FabricationException e) {
      throw exception("Cannot get fabricator type, in order to choose rhythm program", e);
    }

    switch (type) {
      case Continue:
        Optional<Program> selectedPreviously = getRhythmProgramSelectedPreviouslyForSegmentMemeConstellation();
        return selectedPreviously.isPresent() ? selectedPreviously.get() : chooseFreshRhythm();

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
   wherein the current pattern of the selected main program
   has a non-unique (previously encountered) meme constellation
   <p>
   Compute the pattern-meme constellations of any previous segments which selected the same main program
   <p>
   [#161736024] for each unique program-pattern-meme constellation within the main program

   @return rhythm program if previously selected, or null if none is found
   */
  private Optional<Program> getRhythmProgramSelectedPreviouslyForSegmentMemeConstellation() {
    try {
      Map<String, UUID> constellationProgramIds = Maps.newHashMap();
      String con = fabricator.getMemeIsometryOfSegment().getConstellation();
      if (fabricator.getMemeConstellationChoicesOfPreviousSegments().containsKey(con)) {
        for (SegmentChoice choice : fabricator.getMemeConstellationChoicesOfPreviousSegments().get(con)) {
          if (ProgramType.Rhythm == choice.getType())
            constellationProgramIds.put(con, fabricator.getProgram(choice).getId());
        }
      }
      String constellation = MemeIsometry.ofMemes(fabricator.getSegmentMemes()).getConstellation();
      return constellationProgramIds.containsKey(constellation) ? Optional.of(fabricator.getSourceMaterial().getProgram(constellationProgramIds.get(constellation))) : Optional.empty();

    } catch (FabricationException e) {
      log.warn(formatLog("Could not get rhythm program selected previously for segment meme constellation"), e);
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

   @return rhythm sequence if previously selected, or null if none is found
   */
  private Optional<UUID> getPreviousVoiceInstrumentId(String segmentMemeConstellation, UUID voiceId) {
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
   Transposition for rhythm-type sequence choice for segment

   @param rhythmProgram to get transpose of
   @return +/- semitones transposition of rhythm-type sequence choice
   */
  private Integer computeRhythmTranspose(Program rhythmProgram) {
    return Key.delta(rhythmProgram.getKey(), fabricator.getSegment().getKey(), 0);
  }

  /**
   Choose a fresh rhythm based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Rhythm-Program must match the `minor` or `major` with the Key of the current Segment.

   @return rhythm-type Program
   @throws CraftException on failure
   <p>
   future: actually choose rhythm program
   */
  private Program chooseFreshRhythm() throws CraftException {
    EntityRank<Program> superEntityRank = new EntityRank<>();

    // (2) retrieve programs bound to chain
    Collection<Program> sourcePrograms = fabricator.getSourceMaterial().getProgramsOfType(ProgramType.Rhythm);

    // (3) score each source program based on meme isometry
    for (Program program : sourcePrograms) {
      Optional<Double> score = getRhythmScoreOf(program);
      score.ifPresent(aDouble -> superEntityRank.add(program, aDouble));
    }

    // report
    fabricator.putReport("rhythmChoice", superEntityRank.report());

    // (4) return the top choice
    try {
      return superEntityRank.getTop();
    } catch (FabricationException e) {
      throw exception("Found no rhythm-type program bound to Chain!", e);
    }
  }

  /**
   Score a candidate for rhythm program, given current fabricator
   Score includes matching memes, previous segment to macro program first pattern
   <p>
   Returns ZERO if the program has no memes, in order to fix:
   [#162040109] Artist expects program with no memes will never be selected for chain craft.

   @param program to score
   @return score, including +/- entropy, -1 if this program has no memes
   */
  private Optional<Double> getRhythmScoreOf(Program program) {
    try {
      Collection<MemeEntity> memes = fabricator.getSourceMaterial().getMemesAtBeginning(program);
      if (!memes.isEmpty())
        return Optional.of(fabricator.getMemeIsometryOfSegment().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY));

    } catch (Exception e) {
      log.warn("[segId={}] While scoring rhythm {}", fabricator.getSegment().getId(), program, e);
    }
    return Optional.empty();
  }


  /**
   craft segment events for one rhythm voice
   [#161736024] if segment meme constellation already encountered, use that instrument-voice

   @param voice to craft events for
   @throws CraftException on failure
   */
  private void craftArrangementForRhythmVoice(ProgramSequence sequence, SegmentChoice choice, ProgramVoice voice, Map<String, InstrumentAudio> previousInstrumentAudio) throws CraftException {
    try {
      String constellation = MemeIsometry.ofMemes(fabricator.getSegmentMemes()).getConstellation();
      Optional<UUID> instrumentId = getPreviousVoiceInstrumentId(constellation, voice.getId());

      // if no previous instrument found, choose a fresh one
      SegmentChoiceArrangement arrangement = fabricator.add(SegmentChoiceArrangement.create(choice)
        .setProgramVoiceId(voice.getId())
        .setInstrumentId(instrumentId.isPresent() ? instrumentId.get() : chooseFreshPercussiveInstrument(voice).getId()));

      // choose intro pattern (if available)
      Optional<ProgramSequencePattern> introPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePatternType.Intro);

      // choose outro pattern (if available)
      Optional<ProgramSequencePattern> outroPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePatternType.Outro);

      // compute in and out points, and length # beats for which loop patterns will be required
      long loopOutPos = fabricator.getSegment().getTotal() - (outroPattern.isPresent() ? outroPattern.get().getTotal() : 0);

      // begin at the beginning and fabricate events for the segment of beginning to end
      double curPos = 0.0;

      // if intro pattern, fabricate those voice event first
      if (introPattern.isPresent())
        curPos += craftRhythmPatternPatternEvents(previousInstrumentAudio, choice, arrangement, introPattern.get(), curPos, loopOutPos, 0);

      // choose loop patterns until arrive at the out point or end of segment
      while (curPos < loopOutPos) {
        Optional<ProgramSequencePattern> loopPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePatternType.Loop);
        if (loopPattern.isPresent())
          curPos += craftRhythmPatternPatternEvents(previousInstrumentAudio, choice, arrangement, loopPattern.get(), curPos, loopOutPos, 0);
        else
          curPos = loopOutPos;
      }

      // "Go for it" more towards the end of a program (and only during the outro, when present)
      double goForItRatio = fabricator.getSequenceBindingOffsetForChoice(fabricator.getCurrentMainChoice()).doubleValue() /
        fabricator.getMaxAvailableSequenceBindingOffset(fabricator.getCurrentMainChoice()).doubleValue();

      // if outro pattern, fabricate those voice event last
      // [#161466708] compute how much to go for it in the outro
      if (outroPattern.isPresent())
        craftRhythmPatternPatternEvents(previousInstrumentAudio, choice, arrangement, outroPattern.get(), curPos, loopOutPos, goForItRatio);

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
  private Instrument chooseFreshPercussiveInstrument(ProgramVoice voice) throws CraftException {
    EntityRank<Instrument> superEntityRank = new EntityRank<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.getSourceMaterial().getInstrumentsOfType(InstrumentType.Percussive);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use: {}", fabricator.getSegment().getId(), voice);

    // (3) score each source instrument based on meme isometry
    for (Instrument instrument : sourceInstruments) {
      try {
        superEntityRank.add(instrument, scorePercussiveInstrument(instrument));
      } catch (Exception e) {
        log.debug("[segId={}] while scoring percussive instrument", fabricator.getSegment().getId(), e);
      }
    }

    // report
    fabricator.putReport("percussiveChoice", superEntityRank.report());

    // (4) return the top choice
    try {
      return superEntityRank.getTop();
    } catch (FabricationException e) {
      throw exception("Found no percussive-type instrument bound to Chain!", e);
    }
  }

  /**
   Score a candidate for percussive instrument, given current fabricator

   @param instrument to score
   @return score, including +/- entropy
   */
  private double scorePercussiveInstrument(Instrument instrument) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_INSTRUMENT_ENTROPY);

    // Score includes matching memes, previous segment to macro instrument first pattern
    try {
      score += fabricator.getMemeIsometryOfSegment().score(fabricator.getSourceMaterial().getMemes(instrument)) * SCORE_MATCHED_MEMES;
    } catch (FabricationException e) {
      throw exception(String.format("Could not score percussive instrumentId=%s", instrument.getId()), e);
    }

    return score;
  }

  /**
   Craft the voice events of a single rhythm pattern.
   [#161601279] Artist during rhythm craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

   @param previousInstrumentAudio map of previous instrument audio of which to potentially select
   @param choice                  to craft pattern events for
   @param arrangement             to craft pattern events for
   @param pattern                 to source events
   @param fromPos                 to write events to segment
   @param maxPos                  to write events to segment
   @param goForItRatio            entropy is increased during the progression of a main sequence [#161466708]
   @return deltaPos of start, after crafting this batch of rhythm pattern events
   */
  private double craftRhythmPatternPatternEvents(Map<String, InstrumentAudio> previousInstrumentAudio, SegmentChoice choice, SegmentChoiceArrangement arrangement, ProgramSequencePattern pattern, double fromPos, double maxPos, double goForItRatio) throws CraftException {
    if (Objects.isNull(pattern)) throw exception("Cannot craft create null pattern");
    double totalPos = maxPos - fromPos;
    Collection<ProgramSequencePatternEvent> events = fabricator.getSourceMaterial().getEvents(pattern);
    Instrument instrument = fabricator.getSourceMaterial().getInstrument(arrangement.getInstrumentId());
    for (ProgramSequencePatternEvent event : events) {
      double chanceOfRandomChoice = 0.0 == goForItRatio ? 0.0 : goForItRatio * Value.ratio(event.getPosition() - fromPos, totalPos);
      pickInstrumentAudio(previousInstrumentAudio, instrument, arrangement, event, choice.getTranspose(), fromPos, chanceOfRandomChoice);
    }
    return Math.min(totalPos, pattern.getTotal());

  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param arrangement   to of pick within@param previousInstrumentAudio

   @param previousInstrumentAudio map of previous instrument audio of which to potentially select
   @param event                   to pick audio for
   @param shiftPosition           offset voice event zero within current segment
   @param chanceOfRandomChoice    entropy is increased during the progression of a main sequence [#161466708]
   */
  private void pickInstrumentAudio(Map<String, InstrumentAudio> previousInstrumentAudio, Instrument instrument, SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent event, int transpose, Double shiftPosition, Double chanceOfRandomChoice) throws CraftException {
    try {
      InstrumentAudio audio = selectInstrumentAudio(previousInstrumentAudio, instrument, event, chanceOfRandomChoice);

      // Morph & Point attributes are expressed in beats
      double position = event.getPosition() + shiftPosition;
      double duration = event.getDuration();
      Chord chord = fabricator.getChordAt((int) Math.floor(position));

      // The final note is transformed based on instrument type
      Note note = pickNote(
        Note.of(event.getNote()).transpose(transpose),
        chord, audio, instrument.getType());

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.computeSecondsAtPosition(position);
      double lengthSeconds = fabricator.computeSecondsAtPosition(position + duration) - startSeconds;

      // of pick
      fabricator.add(SegmentChoiceArrangementPick.create(segmentChoiceArrangement)
        .setInstrumentAudioId(audio.getId())
        .setProgramSequencePatternEventId(event.getId())
        .setName(getTrackName(event))
        .setStart(startSeconds)
        .setLength(lengthSeconds)
        .setAmplitude(event.getVelocity())
        .setPitch(fabricator.getPitch(note)));

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
      EntityRank<InstrumentAudio> audioEntityRank = new EntityRank<>();

      // add all audio to chooser
      audioEntityRank.addAll(fabricator.getSourceMaterial().getAudios(instrument));

      // score each audio against the current voice event, with some variability
      for (InstrumentAudioEvent audioEvent : fabricator.getSourceMaterial().getFirstEventsOfAudiosOfInstrument(instrument))
        audioEntityRank.score(audioEvent.getInstrumentAudioId(),
          Chance.normallyAround(
            NameIsometry.similarity(getTrackName(event), audioEvent.getName()),
            SCORE_INSTRUMENT_ENTROPY));

      // final chosen audio event
      return audioEntityRank.getTop();

    } catch (FabricationException e) {
      throw exception(String.format("No acceptable Audio found for instrumentId=%s, eventId=%s", instrument.getId(), event.getId()), e);
    }
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   [#295] Pitch of percussive-type instrument audio is altered the least # semitones possible to conform to the current chord

   @param fromNote       of voice event
   @param chord          current
   @param audio          that has been picked
   @param instrumentType of instrument
   @return final note
   */
  private Note pickNote(Note fromNote, Chord chord, InstrumentAudio audio, InstrumentType instrumentType) {
    if (InstrumentType.Percussive == instrumentType) {
      return fabricator.getNoteAtPitch(audio.getPitch())
        .conformedTo(chord);
    } else {
      return fromNote
        .conformedTo(chord);
    }
  }

}
