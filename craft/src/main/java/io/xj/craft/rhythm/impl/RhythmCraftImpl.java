//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.isometry.EventIsometry;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.isometry.SubEntityRank;
import io.xj.core.isometry.SuperEntityRank;
import io.xj.core.model.entity.Event;
import io.xj.core.model.entity.Meme;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.program.PatternType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.PatternEvent;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.Voice;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.util.Chance;
import io.xj.core.util.Value;
import io.xj.craft.CraftImpl;
import io.xj.craft.exception.CraftException;
import io.xj.craft.rhythm.RhythmCraft;
import io.xj.music.Chord;
import io.xj.music.Key;
import io.xj.music.Note;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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
  private static String eventKey(Pick pick) {
    return String.format(KEY_VOICE_NAME_TEMPLATE, pick.getVoiceId(), pick.getName());
  }

  /**
   Key for any pattern event designed to collide at same voice id + name

   @param patternEvent to get key of
   @return unique key for pattern event
   */
  private String eventKey(PatternEvent patternEvent) throws CraftException {
    return String.format(KEY_VOICE_NAME_TEMPLATE, getVoiceId(patternEvent), patternEvent.getName());
  }

  /**
   Get the Voice UUID of a given pattern event

   @param patternEvent to get voice UUID of
   @return Voice UUID
   @throws CraftException on failure to get Voice UUID
   */
  private UUID getVoiceId(PatternEvent patternEvent) throws CraftException {
    try {
      return fabricator.getSourceMaterial()
        .getProgram(patternEvent.getProgramId())
        .getPattern(patternEvent.getPatternId())
        .getVoiceId();
    } catch (CoreException e) {
      throw new CraftException(String.format("Unable to get Voice ID for patternEventId=%s", patternEvent.getId()), e);
    }
  }

  @Override
  public void doWork() throws CraftException {
    Map<String, Audio> previousInstrumentAudio = getPreviousInstrumentAudio();
    try {

      // program
      Program rhythmProgram = chooseRhythmProgram();
      Choice rhythmChoice = fabricator.add(new Choice()
        .setType(ProgramType.Rhythm.toString())
        .setProgramId(rhythmProgram.getId())
        .setTranspose(computeRhythmTranspose(rhythmProgram)));

      // rhythm sequence is selected at random from the current program
      // FUTURE: [#166855956] Rhythm Program with multiple Sequences
      Sequence rhythmSequence = fabricator.getSequence(rhythmChoice);

      // voice arrangements
      for (Voice voice : rhythmProgram.getVoices())
        craftArrangementForRhythmVoice(rhythmProgram, rhythmSequence, rhythmChoice, voice, previousInstrumentAudio);

      // Finally, update the segment with the crafted content
      fabricator.updateSegment();

    } catch (CoreException e) {
      throw exception("Could not update segment", e);
    }
  }

  /**
   Get previously chosen (for previous segments with same main program and meme constellation) instrument audio

   @return map of previous chosen instrument audio
   @throws CraftException on failure to build map
   */
  private Map<String, Audio> getPreviousInstrumentAudio() throws CraftException {
    Map<String, Audio> previousInstrumentAudio = Maps.newHashMap();
    try {
      String con = fabricator.getMemeIsometryOfSegment().getConstellation();
      if (fabricator.getMemeConstellationPicksOfPreviousSegments().containsKey(con)) {
        Collection<Pick> picks = fabricator.getMemeConstellationPicksOfPreviousSegments().get(con);
        log.info("[segId={}] previous meme constellation picks {}", fabricator.getSegment().getId(), picks.size());
        for (Pick pick : picks) {
          String key = eventKey(pick);
          previousInstrumentAudio.put(key, fabricator.getAudio(pick));
        }
      }
    } catch (CoreException e) {
      throw new CraftException("Unable to build map of previous instrument audio", e);
    }
    return previousInstrumentAudio;
  }

  /**
   compute (and cache) the mainProgram

   @return mainProgram
   */
  private Program chooseRhythmProgram() throws CraftException {
    FabricatorType type;
    try {
      type = fabricator.getType();
    } catch (CoreException e) {
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
      Map<String, BigInteger> constellationProgramIds = Maps.newHashMap();
      String con = fabricator.getMemeIsometryOfSegment().getConstellation();
      if (fabricator.getMemeConstellationChoicesOfPreviousSegments().containsKey(con)) {
        for (Choice choice : fabricator.getMemeConstellationChoicesOfPreviousSegments().get(con)) {
          if (ProgramType.Rhythm == choice.getType())
            constellationProgramIds.put(con, fabricator.getProgram(choice).getId());
        }
      }
      String constellation = MemeIsometry.ofMemes(fabricator.getSegment().getMemes()).getConstellation();
      return constellationProgramIds.containsKey(constellation) ? Optional.of(fabricator.getSourceMaterial().getProgram(constellationProgramIds.get(constellation))) : Optional.empty();

    } catch (CoreException e) {
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
  private Optional<BigInteger> getPreviousVoiceInstrumentId(String segmentMemeConstellation, UUID voiceId) {
    try {
      for (String constellation : fabricator.getMemeConstellationArrangementsOfPreviousSegments().keySet()) {
        if (Objects.equals(segmentMemeConstellation, constellation))
          for (Arrangement arrangement : fabricator.getMemeConstellationArrangementsOfPreviousSegments().get(constellation)) {
            if (Objects.equals(voiceId, arrangement.getVoiceId()))
              return Optional.of(arrangement.getInstrumentId());
          }
      }

    } catch (CoreException e) {
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
    SuperEntityRank<Program> superEntityRank = new SuperEntityRank<>();

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
    } catch (CoreException e) {
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
      Collection<Meme> memes = program.getMemesAtBeginning();
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
  private void craftArrangementForRhythmVoice(Program program, Sequence sequence, Choice choice, Voice voice, Map<String, Audio> previousInstrumentAudio) throws CraftException {
    try {
      String constellation = MemeIsometry.ofMemes(fabricator.getSegment().getMemes()).getConstellation();
      Optional<BigInteger> instrumentId = getPreviousVoiceInstrumentId(constellation, voice.getId());

      // if no previous instrument found, choose a fresh one
      Arrangement arrangement = fabricator.add(new Arrangement()
        .setChoiceId(choice.getId())
        .setVoiceId(voice.getId())
        .setInstrumentId(instrumentId.isPresent() ? instrumentId.get() : chooseFreshPercussiveInstrument(voice).getId()));

      // choose intro pattern (if available)
      Optional<Pattern> introPattern = program.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, PatternType.Intro);

      // choose outro pattern (if available)
      Optional<Pattern> outroPattern = program.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, PatternType.Outro);

      // compute in and out points, and length # beats for which loop patterns will be required
      long loopOutPos = fabricator.getSegment().getTotal() - (outroPattern.isPresent() ? outroPattern.get().getTotal() : 0);

      // begin at the beginning and fabricate events for the segment from beginning to end
      double curPos = 0.0;

      // if intro pattern, fabricate those voice event first
      if (introPattern.isPresent())
        curPos += craftRhythmPatternPatternEvents(previousInstrumentAudio, program, choice, arrangement, introPattern.get(), curPos, loopOutPos, 0);

      // choose loop patterns until arrive at the out point or end of segment
      while (curPos < loopOutPos) {
        Optional<Pattern> loopPattern = program.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, PatternType.Loop);
        if (loopPattern.isPresent())
          curPos += craftRhythmPatternPatternEvents(previousInstrumentAudio, program, choice, arrangement, loopPattern.get(), curPos, loopOutPos, 0);
        else
          curPos = loopOutPos;
      }

      // "Go for it" more towards the end of a program (and only during the outro, when present)
      double goForItRatio = fabricator.getSequenceBindingOffsetForChoice(fabricator.getCurrentMainChoice()).doubleValue() /
        fabricator.getMaxAvailableSequenceBindingOffset(fabricator.getCurrentMainChoice()).doubleValue();

      // if outro pattern, fabricate those voice event last
      // [#161466708] compute how much to go for it in the outro
      if (outroPattern.isPresent())
        craftRhythmPatternPatternEvents(previousInstrumentAudio, program, choice, arrangement, outroPattern.get(), curPos, loopOutPos, goForItRatio);

    } catch (CoreException e) {
      throw exception(String.format("Failed to craft arrangement for rhythm voiceId=%s", voice.getId()), e);
    }
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws CraftException on failure
   */
  private Instrument chooseFreshPercussiveInstrument(Voice voice) throws CraftException {
    SuperEntityRank<Instrument> superEntityRank = new SuperEntityRank<>();

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
    } catch (CoreException e) {
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
      score += fabricator.getMemeIsometryOfSegment().score(instrument.getMemes()) * SCORE_MATCHED_MEMES;
    } catch (CoreException e) {
      throw exception(String.format("Could not score percussive instrumentId=%s", instrument.getId()), e);
    }

    return score;
  }

  /**
   Craft the voice events of a single rhythm pattern.
   [#161601279] Artist during rhythm craft audio selection wants randomness of outro audio selection to gently ramp from zero to N over the course of the outro.

   @param previousInstrumentAudio map of previous instrument audio from which to potentially select
   @param program                 to craft pattern events from
   @param choice                  to craft pattern events for
   @param arrangement             to craft pattern events for
   @param pattern                 to source events
   @param fromPos                 to write events to segment
   @param maxPos                  to write events to segment
   @param goForItRatio            entropy is increased during the progression of a main sequence [#161466708]
   @return deltaPos from start, after crafting this batch of rhythm pattern events
   */
  private double craftRhythmPatternPatternEvents(Map<String, Audio> previousInstrumentAudio, Program program, Choice choice, Arrangement arrangement, Pattern pattern, double fromPos, double maxPos, double goForItRatio) throws CraftException {
    try {
      if (Objects.isNull(pattern)) throw exception("Cannot craft from null pattern");
      double totalPos = maxPos - fromPos;
      Collection<PatternEvent> patternEvents = program.getEventsForPattern(pattern);
      Instrument instrument = fabricator.getSourceMaterial().getInstrument(arrangement.getInstrumentId());
      for (PatternEvent patternEvent : patternEvents) {
        double chanceOfRandomChoice = 0.0 == goForItRatio ? 0.0 : goForItRatio * Value.ratio(patternEvent.getPosition() - fromPos, totalPos);
        pickInstrumentAudio(previousInstrumentAudio, instrument, arrangement, patternEvent, choice.getTranspose(), fromPos, chanceOfRandomChoice);
      }
      return Math.min(totalPos, pattern.getTotal());

    } catch (CoreException e) {
      throw exception(String.format("Could not craft rhythm pattern patternEvents fromPos=%f, patternId=%s, maxPos=%f, goForItRatio=%f",
        fromPos, pattern.getId(), maxPos, goForItRatio), e);
    }
  }

  /**
   create a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param arrangement   to create pick within@param previousInstrumentAudio

   @param previousInstrumentAudio map of previous instrument audio from which to potentially select
   @param patternEvent            to pick audio for
   @param shiftPosition           offset voice event zero within current segment
   @param chanceOfRandomChoice    entropy is increased during the progression of a main sequence [#161466708]
   */
  private void pickInstrumentAudio(Map<String, Audio> previousInstrumentAudio, Instrument instrument, Arrangement arrangement, PatternEvent patternEvent, int transpose, Double shiftPosition, Double chanceOfRandomChoice) throws CraftException {
    try {
      Audio audio = selectInstrumentAudio(previousInstrumentAudio, instrument, patternEvent, chanceOfRandomChoice);

      // Morph & Point attributes are expressed in beats
      double position = patternEvent.getPosition() + shiftPosition;
      double duration = patternEvent.getDuration();
      Chord chord = fabricator.getChordAt((int) Math.floor(position));

      // The final note is transformed based on instrument type
      Note note = pickNote(
        Note.of(patternEvent.getNote()).transpose(transpose),
        chord, audio, instrument.getType());

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.computeSecondsAtPosition(position);
      double lengthSeconds = fabricator.computeSecondsAtPosition(position + duration) - startSeconds;

      // create pick
      fabricator.add(new Pick()
        .setArrangementId(arrangement.getId())
        .setAudioId(audio.getId())
        .setVoiceId(arrangement.getVoiceId())
        .setPatternEventId(patternEvent.getId())
        .setName(patternEvent.getName())
        .setStart(startSeconds)
        .setLength(lengthSeconds)
        .setAmplitude(patternEvent.getVelocity())
        .setPitch(fabricator.getPitch(note)));

    } catch (CoreException e) {
      throw exception(String.format("Could not pick audio for instrumentId=%s, arrangementId=%s, patternEventId=%s, transpose=%d, shiftPosition=%f, chanceOfRandomChoice=%f",
        instrument.getId(), arrangement.getId(), patternEvent.getId(), transpose, shiftPosition, chanceOfRandomChoice), e);
    }
  }

  /**
   Determine if we will use a cached or new audio for this selection
   Cached audio defaults to random selection if none has been previously encountered

   @param previousInstrumentAudio map of previous instrument audio from which to potentially select
   @param instrument              from which to score available audios, and make a selection
   @param patternEvent            to match
   @param chanceOfRandomChoice    from 0 to 1, chance that a random audio will be selected (instead of the cached selection)
   @return matched new audio
   @throws CraftException on failure
   */
  private Audio selectInstrumentAudio(Map<String, Audio> previousInstrumentAudio, Instrument instrument, PatternEvent patternEvent, Double chanceOfRandomChoice) throws CraftException {
    if (0 < chanceOfRandomChoice && random.nextDouble() <= chanceOfRandomChoice) {
      return selectNewInstrumentAudio(instrument, patternEvent);
    } else {
      return selectPreviousInstrumentAudio(previousInstrumentAudio, instrument, patternEvent);
    }
  }

  /**
   Select the cached (already selected for this segment+drum name)
   instrument audio based on a pattern event.
   <p>
   If never encountered, default to new selection and cache that.

   @param previousInstrumentAudio map of previous instrument audio from which to potentially select
   @param instrument              from which to score available audios, and make a selection
   @param patternEvent            to match
   @return matched new audio
   @throws CraftException on failure
   */
  private Audio selectPreviousInstrumentAudio(Map<String, Audio> previousInstrumentAudio, Instrument instrument, PatternEvent patternEvent) throws CraftException {
    String key = eventKey(patternEvent);
    if (!previousInstrumentAudio.containsKey(key))
      previousInstrumentAudio.put(key, selectNewInstrumentAudio(instrument, patternEvent));
    return previousInstrumentAudio.get(key);
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument   from which to score available audios, and make a selection
   @param patternEvent to match
   @return matched new audio
   @throws CraftException on failure
   */
  private Audio selectNewInstrumentAudio(Instrument instrument, Event patternEvent) throws CraftException {
    try {
      SubEntityRank<Audio> audioSubEntityRank = new SubEntityRank<>();

      // add all audio to chooser
      audioSubEntityRank.addAll(instrument.getAudios());

      // score each audio against the current voice event, with some variability
      for (AudioEvent audioEvent : fabricator.getFirstEventsOfAudiosOfInstrument(instrument))
        audioSubEntityRank.score(audioEvent.getAudioId(),
          Chance.normallyAround(
            EventIsometry.similarity(patternEvent, audioEvent),
            SCORE_INSTRUMENT_ENTROPY));

      // final chosen audio event
      return audioSubEntityRank.getTop();

    } catch (CoreException e) {
      throw exception(String.format("No acceptable Audio found for instrumentId=%s, patternEventId=%s", instrument.getId(), patternEvent.getId()), e);
    }
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   [#295] Pitch of percussive-type instrument audio is altered the least # semitones possible to conform to the current chord

   @param fromNote       from voice event
   @param chord          current
   @param audio          that has been picked
   @param instrumentType of instrument
   @return final note
   */
  private Note pickNote(Note fromNote, Chord chord, Audio audio, InstrumentType instrumentType) {
    if (InstrumentType.Percussive == instrumentType) {
      return fabricator.getNoteAtPitch(audio.getPitch())
        .conformedTo(chord);
    } else {
      return fromNote
        .conformedTo(chord);
    }
  }

}
