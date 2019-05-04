//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.isometry.EventIsometry;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.event.Event;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.voice.Voice;
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

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 Rhythm craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class RhythmCraftImpl extends CraftImpl implements RhythmCraft {
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private static final String KEY_VOICE_INFLECTION_TEMPLATE = "%s_%s";
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);
  private final Map<String, Audio> previousInstrumentAudio = Maps.newConcurrentMap();
  private final SecureRandom random = new SecureRandom();

  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    setFabricator(fabricator);
  }

  /**
   Unique event key for any pattern event (by voice id and inflection)

   @param patternEvent to get key of
   @return unique key for pattern event
   */
  private static String eventKey(PatternEvent patternEvent) {
    return String.format(KEY_VOICE_INFLECTION_TEMPLATE, patternEvent.getVoiceId(), patternEvent.getInflection());
  }

  /**
   Unique key for any pattern event (by voice id and inflection)

   @param pick to get key of
   @return unique key for pattern event
   */
  private static String eventKey(Pick pick) {
    return String.format(KEY_VOICE_INFLECTION_TEMPLATE, pick.getVoiceId(), pick.getInflection());
  }

  @Override
  public void doWork() throws CraftException {
    parseRelevantPreviousSegments();
    craftRhythm();
    craftRhythmVoiceArrangements();
    craftRhythmPatterns();
    try {
      getFabricator().updateSegment();
    } catch (CoreException e) {

      throw exception("Could not update segment", e);
    }
  }

  /**
   [#162361534] Artist wants segments that continue the use of a main sequence
   from fabricator, check for presence of a relevant segment from which we ought to draw all the previously picked instrument audio
   if relevant, use all picks of previous segment to bootstrap the previously picked instrument audio
   */
  private void parseRelevantPreviousSegments() throws CraftException {
    try {
      String con = getFabricator().getMemeIsometryOfSegment().getConstellation();
      if (getFabricator().getMemeConstellationPicksOfPreviousSegment().containsKey(con)) {
        Collection<Pick> picks = getFabricator().getMemeConstellationPicksOfPreviousSegment().get(con);
        log.info("[segId={}] previous meme constellation picks {}", getFabricator().getSegment().getId(), picks.size());
        for (Pick pick : picks) {
          String key = eventKey(pick);
          previousInstrumentAudio.put(key, getFabricator().getSourceMaterial().getAudio(pick.getAudioId()));
        }
      }

    } catch (CoreException e) {
      throw exception("Could not parse relevant previous segments", e);
    }
  }

  /**
   craft segment rhythm
   */
  private void craftRhythm() throws CraftException {
    // first trigger the selection of a rhythm sequence
    Sequence sequence = chooseRhythmSequence();
    // then save it as a choice
    try {
      getFabricator().add(new Choice()
        .setSegmentId(getFabricator().getSegment().getId())
        .setType(SequenceType.Rhythm.toString())
        .setSequenceId(sequence.getId())
        .setTranspose(getRhythmTranspose()));

    } catch (CoreException e) {
      throw exception(String.format("Could not add rhythm choice of sequenceId=%s", sequence.getId()), e);
    }
  }

  /**
   compute (and cache) the mainSequence

   @return mainSequence
   */
    private Sequence chooseRhythmSequence() throws CraftException {
    FabricatorType type;
    try {
      type = getFabricator().getType();
    } catch (CoreException e) {
      throw exception("Cannot get fabricator type, in order to choose rhythm sequence", e);
    }

    switch (type) {
      case Continue:
        Optional<Sequence> selectedPreviously = getRhythmSequenceSelectedPreviouslyForSegmentMemeConstellation();
        return selectedPreviously.isPresent() ? selectedPreviously.get() : chooseFreshRhythm();

      case Initial:
      case NextMain:
      case NextMacro:
        return chooseFreshRhythm();

      default:
        throw exception(String.format("Cannot get Rhythm-type sequence for unknown fabricator type=", type));
    }
  }

  /**
   Determine if a rhythm sequence has been previously selected
   in one of the previous segments of the current main sequence
   wherein the current pattern of the selected main sequence
   has a non-unique (previously encountered) meme constellation
   <p>
   Compute the pattern-meme constellations of any previous segments which selected the same main sequence
   <p>
   [#161736024] for each unique sequence-pattern-meme constellation within the main sequence

   @return rhythm sequence if previously selected, or null if none is found
   */
    private Optional<Sequence> getRhythmSequenceSelectedPreviouslyForSegmentMemeConstellation() {
    try {
      Map<String, BigInteger> constellationSequenceIds = Maps.newConcurrentMap();
      String con = getFabricator().getMemeIsometryOfSegment().getConstellation();
      if (getFabricator().getMemeConstellationChoicesOfPreviousSegment().containsKey(con)) {
        for (Choice choice : getFabricator().getMemeConstellationChoicesOfPreviousSegment().get(con)) {
          if (SequenceType.Rhythm == choice.getType())
            constellationSequenceIds.put(con, getFabricator().getSequenceOfChoice(choice).getId());
        }
      }
      String constellation = MemeIsometry.ofMemes(getFabricator().getSegment().getMemes()).getConstellation();
      return constellationSequenceIds.containsKey(constellation) ? Optional.of(getFabricator().getSourceMaterial().getSequence(constellationSequenceIds.get(constellation))) : Optional.empty();

    } catch (CoreException e) {
      log.warn(formatLog("Could not get rhythm sequence selected previously for segment meme constellation"), e);
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
    private Optional<BigInteger> getPreviousVoiceInstrumentId(String segmentMemeConstellation, BigInteger voiceId) {
    try {
      for (String constellation : getFabricator().getMemeConstellationArrangementsOfPreviousSegment().keySet()) {
        if (Objects.equals(segmentMemeConstellation, constellation))
          for (Arrangement arrangement : getFabricator().getMemeConstellationArrangementsOfPreviousSegment().get(constellation)) {
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

   @return +/- semitones transposition of rhythm-type sequence choice
   */
    private Integer getRhythmTranspose() throws CraftException {
    return Key.delta(chooseRhythmSequence().getKey(), getFabricator().getSegment().getKey(), 0);
  }

  /**
   Choose a fresh rhythm based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Rhythm-Sequence must match the `minor` or `major` with the Key of the current Segment.

   @return rhythm-type Sequence
   @throws CraftException on failure
   <p>
   future: actually choose rhythm sequence
   */
  private Sequence chooseFreshRhythm() throws CraftException {
    EntityRank<Sequence> entityRank = new EntityRank<>();

    // (2) retrieve sequences bound to chain
    Collection<Sequence> sourceSequences;
    try {
      sourceSequences = getFabricator().getSourceMaterial().getSequencesOfType(SequenceType.Rhythm);
    } catch (CoreException e) {
      throw exception("Could not get ingested rhythm sequences", e);
    }

    // (3) score each source sequence based on meme isometry
    for (Sequence sequence : sourceSequences) {
      Optional<Double> score = getRhythmScoreOf(sequence);
      if (score.isPresent()) entityRank.add(sequence, score.get());
    }

    // report
    getFabricator().putReport("rhythmChoice", entityRank.report());

    // (4) return the top choice
    try {
      return entityRank.getTop();
    } catch (CoreException e) {
      throw exception("Found no rhythm-type sequence bound to Chain!", e);
    }
  }

  /**
   Score a candidate for rhythm sequence, given current fabricator
   Score includes matching memes, previous segment to macro sequence first pattern
   <p>
   Returns ZERO if the sequence has no memes, in order to fix:
   [#162040109] Artist expects sequence with no memes will never be selected for chain craft.

   @param sequence to score
   @return score, including +/- entropy, -1 if this sequence has no memes
   */
  private Optional<Double> getRhythmScoreOf(Sequence sequence) {
    try {
      Collection<Meme> memes = getFabricator().getSourceMaterial().getMemesAtBeginningOfSequence(sequence.getId());
      if (!memes.isEmpty())
        return Optional.of(getFabricator().getMemeIsometryOfSegment().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY));

    } catch (Exception e) {
      log.warn("[segId={}] While scoring rhythm {}", getFabricator().getSegment().getId(), sequence, e);
    }
    return Optional.empty();
  }

  /**
   craft segment events for all rhythm voices
   */
  private void craftRhythmVoiceArrangements() throws CraftException {
    try {
      Collection<Arrangement> arrangements = Lists.newArrayList();
      for (Voice voice : getVoicesOfSequence(getFabricator().getSequenceOfChoice(getFabricator().getCurrentRhythmChoice()).getId()))
        arrangements.add(craftArrangementForRhythmVoice(voice));
      getFabricator().setPreArrangementsForChoice(getFabricator().getCurrentRhythmChoice(), arrangements);

    } catch (CoreException e) {
      throw exception("Failed to craft rhythm voice arrangements", e);
    }
  }

  /**
   craft segment events for one rhythm voice
   [#161736024] if segment meme constellation already encountered, use that instrument-voice

   @param voice to craft events for
   @throws CraftException on failure
   */
  private Arrangement craftArrangementForRhythmVoice(Voice voice) throws CraftException {
    try {
      String constellation = MemeIsometry.ofMemes(getFabricator().getSegment().getMemes()).getConstellation();
      Optional<BigInteger> instrumentId = getPreviousVoiceInstrumentId(constellation, voice.getId());

      // if no previous instrument found, choose a fresh one
      return getFabricator().add(new Arrangement()
        .setChoiceUuid(getFabricator().getCurrentRhythmChoice().getUuid())
        .setVoiceId(voice.getId())
        .setInstrumentId(instrumentId.isPresent() ? instrumentId.get() : chooseFreshPercussiveInstrument(voice).getId()));

    } catch (CoreException e) {
      throw exception(String.format("Failed to craft arrangement for rhythm voiceId=", voice.getId()), e);
    }
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same sequence

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws CraftException on failure
   */
  private Instrument chooseFreshPercussiveInstrument(Voice voice) throws CraftException {
    EntityRank<Instrument> entityRank = new EntityRank<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments;
    try {
      sourceInstruments = getFabricator().getSourceMaterial().getInstrumentsOfType(InstrumentType.Percussive);
    } catch (CoreException e) {
      throw exception("Cannot get instruments bound to chain", e);
    }

    // future: [#258] Instrument selection is based on Text Isometry between the voice description and the instrument description
    log.debug("[segId={}] not currently in use: {}", getFabricator().getSegment().getId(), voice);

    // (3) score each source instrument based on meme isometry
    for (Instrument instrument : sourceInstruments) {
      try {
        entityRank.add(instrument, scorePercussiveInstrument(instrument));
      } catch (Exception e) {
        log.debug("[segId={}] while scoring percussive instrument", getFabricator().getSegment().getId(), e);
      }
    }

    // report
    getFabricator().putReport("percussiveChoice", entityRank.report());

    // (4) return the top choice
    try {
      return entityRank.getTop();
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
      score += getFabricator().getMemeIsometryOfSegment().score(getFabricator().getSourceMaterial().getMemesOfInstrument(instrument.getId())) * SCORE_MATCHED_MEMES;
    } catch (CoreException e) {
      throw exception(String.format("Could not score percussive instrumentId=%s", instrument.getId()), e);
    }

    return score;
  }

  /**
   [#153976073] Artist wants Pattern to have type Macro or Main (for Macro- or Main-type sequences), or Intro, Loop, or Outro (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
   [#161466708] Artist wants dynamic randomness over the selection of various audio events to fulfill particular pattern events, in order to establish repetition within any given segment.

   @throws CraftException on failure
   */
  private void craftRhythmPatterns() throws CraftException {
    BigInteger currentRhythmSequenceId;
    try {
      currentRhythmSequenceId = getFabricator().getSequenceOfChoice(getFabricator().getCurrentRhythmChoice()).getId();
    } catch (CoreException e) {
      throw exception("Could not get current rhythm sequence id", e);
    }

    // choose intro pattern (if available)
    @Nullable Pattern introPattern;
    try {
      introPattern = getFabricator().getRandomPatternByType(currentRhythmSequenceId, PatternType.Intro);
    } catch (CoreException ignored) {
      introPattern = null;
    }

    // choose outro pattern (if available)
    @Nullable Pattern outroPattern;
    try {
      outroPattern = getFabricator().getRandomPatternByType(currentRhythmSequenceId, PatternType.Outro);
    } catch (CoreException ignored) {
      outroPattern = null;
    }

    // compute in and out points, and length # beats for which loop patterns will be required
    long loopOutPos = getFabricator().getSegment().getTotal() - (Objects.nonNull(outroPattern) ? outroPattern.getTotal() : 0);

    // begin at the beginning and fabricate events for the segment from beginning to end
    double curPos = 0.0;

    // if intro pattern, fabricate those voice event first
    if (Objects.nonNull(introPattern)) {
      curPos += craftRhythmPatternPatternEvents(curPos, introPattern, loopOutPos, 0);
    }

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < loopOutPos) {
      Pattern loopPattern;
      try {
        loopPattern = getFabricator().getRandomPatternByType(currentRhythmSequenceId, PatternType.Loop);
      } catch (CoreException e) {
        throw exception(String.format("Could not get random loop pattern of sequenceId=%s", currentRhythmSequenceId), e);
      }
      curPos += craftRhythmPatternPatternEvents(curPos, loopPattern, loopOutPos, 0);
    }

    // if outro pattern, fabricate those voice event last
    // [#161466708] compute how much to go for it in the outro
    try {
      if (Objects.nonNull(outroPattern)) {
        double goForItRatio = getFabricator().getSequencePatternOffsetForChoice(getFabricator().getCurrentMainChoice()).doubleValue() /
          getFabricator().getMaxAvailableSequencePatternOffset(getFabricator().getCurrentMainChoice()).doubleValue();
        craftRhythmPatternPatternEvents(curPos, outroPattern, loopOutPos, goForItRatio);
      }
    } catch (CoreException e) {
      throw exception("Could not fabricate rhythm outro", e);
    }
  }

  /**
   Craft the voice events of a single rhythm pattern.
   [#161601279] Artist during rhythm craft audio selection wants randomness of outro audio selection to gently ramp from zero to N over the course of the outro.

   @param fromPos      to write events to segment
   @param pattern      to source events
   @param maxPos       to write events to segment
   @param goForItRatio entropy is increased during the progression of a main sequence [#161466708]
   @return deltaPos from start, after crafting this batch of rhythm pattern events
   */
  private double craftRhythmPatternPatternEvents(double fromPos, Pattern pattern, double maxPos, double goForItRatio) throws CraftException {
    try {
      if (Objects.isNull(pattern)) throw exception("Cannot craft from null pattern");
      double totalPos = maxPos - fromPos;
      Choice choice = getFabricator().getCurrentRhythmChoice();
      Collection<Arrangement> arrangements = getFabricator().getSegment().getArrangementsForChoice(choice);
      for (Arrangement arrangement : arrangements) {
        Collection<PatternEvent> patternEvents = getFabricator().getSourceMaterial().getEventsOfPatternByVoice(pattern.getId(), arrangement.getVoiceId());
        Instrument instrument = getFabricator().getSourceMaterial().getInstrument(arrangement.getInstrumentId());
        for (PatternEvent patternEvent : patternEvents) {
          double chanceOfRandomChoice = 0.0 == goForItRatio ? 0.0 : goForItRatio * Value.ratio(patternEvent.getPosition() - fromPos, totalPos);
          pickInstrumentAudio(instrument, arrangement, patternEvent, choice.getTranspose(), fromPos, chanceOfRandomChoice);
        }
      }
      return Math.min(totalPos, pattern.getTotal());

    } catch (CoreException e) {
      throw exception(String.format("Could not craft rhythm pattern patternEvents fromPos=%f, patternId=%s, maxPos=%f, goForItRatio=%f",
        fromPos, pattern.getId(), maxPos, goForItRatio), e);
    }
  }

  /**
   create a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param arrangement   to create pick within

   @param patternEvent         to pick audio for
   @param shiftPosition        offset voice event zero within current segment
   @param chanceOfRandomChoice entropy is increased during the progression of a main sequence [#161466708]
   */
  private void pickInstrumentAudio(Instrument instrument, Arrangement arrangement, PatternEvent patternEvent, int transpose, Double shiftPosition, Double chanceOfRandomChoice) throws CraftException {
    try {
      Audio audio = selectInstrumentAudio(instrument, patternEvent, chanceOfRandomChoice);

      // Morph & Point attributes are expressed in beats
      double position = patternEvent.getPosition() + shiftPosition;
      double duration = patternEvent.getDuration();
      Chord chord = getFabricator().getChordAt((int) Math.floor(position));

      // The final note is transformed based on instrument type
      Note note = pickNote(
        Note.of(patternEvent.getNote()).transpose(transpose),
        chord, audio, instrument.getType());

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = getFabricator().computeSecondsAtPosition(position);
      double lengthSeconds = getFabricator().computeSecondsAtPosition(position + duration) - startSeconds;

      // create pick
      getFabricator().add(new Pick()
        .setArrangementUuid(arrangement.getUuid())
        .setAudioId(audio.getId())
        .setVoiceId(arrangement.getVoiceId())
        .setPatternEventId(patternEvent.getId())
        .setInflection(patternEvent.getInflection())
        .setStart(startSeconds)
        .setLength(lengthSeconds)
        .setAmplitude(patternEvent.getVelocity())
        .setPitch(getFabricator().getPitch(note)));

    } catch (CoreException e) {
      throw exception(String.format("Could not pick audio for instrumentId=%s, arrangementId=%s, patternEventId=%s, transpose=%d, shiftPosition=%f, chanceOfRandomChoice=%f",
        instrument.getId(), arrangement.getUuid(), patternEvent.getId(), transpose, shiftPosition, chanceOfRandomChoice), e);
    }
  }

  /**
   Determine if we will use a cached or new audio for this selection
   Cached audio defaults to random selection if none has been previously encountered

   @param instrument           from which to score available audios, and make a selection
   @param patternEvent         to match
   @param chanceOfRandomChoice from 0 to 1, chance that a random audio will be selected (instead of the cached selection)
   @return matched new audio
   @throws CraftException on failure
   */
  private Audio selectInstrumentAudio(Instrument instrument, PatternEvent patternEvent, Double chanceOfRandomChoice) throws CraftException {
    if (0 < chanceOfRandomChoice && random.nextDouble() <= chanceOfRandomChoice) {
      return selectNewInstrumentAudio(instrument, patternEvent);
    } else {
      return selectPreviousInstrumentAudio(instrument, patternEvent);
    }
  }

  /**
   Select the cached (already selected for this segment+drum inflection)
   instrument audio based on a pattern event.
   <p>
   If never encountered, default to new selection and cache that.

   @param instrument   from which to score available audios, and make a selection
   @param patternEvent to match
   @return matched new audio
   @throws CraftException on failure
   */
  private Audio selectPreviousInstrumentAudio(Instrument instrument, PatternEvent patternEvent) throws CraftException {
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
      EntityRank<Audio> audioEntityRank = new EntityRank<>();

      // add all audio to chooser
      audioEntityRank.addAll(getFabricator().getSourceMaterial().getAudiosOfInstrument(instrument.getId()));

      // score each audio against the current voice event, with some variability
      for (AudioEvent audioEvent : getFabricator().getSourceMaterial().getFirstEventsOfAudiosOfInstrument(instrument.getId()))
        audioEntityRank.score(audioEvent.getAudioId(),
          Chance.normallyAround(
            EventIsometry.similarity(patternEvent, audioEvent),
            SCORE_INSTRUMENT_ENTROPY));

      // final chosen audio event
      return audioEntityRank.getTop();

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
      return getFabricator().getNoteAtPitch(audio.getPitch())
        .conformedTo(chord);
    } else {
      return fromNote
        .conformedTo(chord);
    }
  }

  /**
   all voices in current pattern of chosen rhythm-type sequence

   @param sequenceId to get voices of
   @return voices for sequence
   @throws CraftException on failure
   */
  private Iterable<Voice> getVoicesOfSequence(BigInteger sequenceId) throws CraftException {
    try {
      List<Voice> voices = Lists.newArrayList();
      voices.addAll(getFabricator().getSourceMaterial().getVoicesOfSequence(sequenceId));
      return voices;

    } catch (CoreException e) {
      throw exception(String.format("Could not get voices for sequenceId=%s", sequenceId), e);
    }
  }

}
