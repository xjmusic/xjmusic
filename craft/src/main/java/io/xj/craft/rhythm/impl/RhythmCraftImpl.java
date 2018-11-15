// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.exception.BusinessException;
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
import io.xj.craft.basis.Basis;
import io.xj.craft.isometry.EventIsometry;
import io.xj.craft.isometry.MemeIsometry;
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

/**
 Rhythm craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class RhythmCraftImpl implements RhythmCraft {
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private final Basis basis;
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);
  private final Map<String, Audio> cachedSelectionInstrumentAudio = Maps.newConcurrentMap();
  private final SecureRandom random = new SecureRandom();
  private BigInteger _rhythmPatternOffset;
  private Sequence _rhythmSequence;


  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Basis basis
    /*-*/) {
    this.basis = basis;
  }

  /**
   Unique key for any pattern event (by voice id and inflection)

   @param patternEvent to get key of
   @return unique key for pattern event
   */
  private static String patternEventKey(PatternEvent patternEvent) {
    return String.format("%s_%s", patternEvent.getVoiceId(), patternEvent.getInflection());
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      craftRhythm();
      craftRhythmVoiceArrangements();
      craftRhythmPatterns();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type RhythmCraft for segment #%s",
          basis.type(), basis.segment().getId().toString()), e);
    }
  }

  /**
   craft segment rhythm
   */
  private void craftRhythm() throws Exception {
    Sequence sequence = rhythmSequence();
    if (Objects.nonNull(sequence))
      basis.create(
        new Choice()
          .setSegmentId(basis.segment().getId())
          .setType(SequenceType.Rhythm.toString())
          .setSequenceId(sequence.getId())
          .setTranspose(rhythmTranspose())
          .setSequencePatternOffset(rhythmPatternOffset()));
  }

  /**
   compute (and cache) the mainSequence

   @return mainSequence
   */
  private Sequence rhythmSequence() throws Exception {
    if (Objects.isNull(_rhythmSequence))
      switch (basis.type()) {

        case Continue:
          Sequence selectedPreviously = rhythmSequenceSelectedPreviouslyForSegmentMemeConstellation();
          _rhythmSequence = Objects.nonNull(selectedPreviously) ? selectedPreviously : selectFreshRhythm();

          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmSequence = selectFreshRhythm();
      }

    return _rhythmSequence;
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
  @Nullable
  private Sequence rhythmSequenceSelectedPreviouslyForSegmentMemeConstellation() throws Exception {
    Map<String, BigInteger> constellationSequenceIds = Maps.newConcurrentMap();
    for (String constellation : basis.previousSegmentMemeConstellationChoices().keySet()) {
      for (Choice choice : basis.previousSegmentMemeConstellationChoices().get(constellation)) {
        if (Objects.equals(SequenceType.Rhythm, choice.getType()))
          constellationSequenceIds.put(constellation, choice.getSequenceId());
      }
    }
    String constellation = MemeIsometry.ofMemes(basis.segmentMemes()).getConstellation();
    return constellationSequenceIds.containsKey(constellation) ? basis.sequence(constellationSequenceIds.get(constellation)) : null;
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
  @Nullable
  private BigInteger previousVoiceInstrumentId(String segmentMemeConstellation, BigInteger voiceId) throws Exception {
    for (String constellation : basis.previousSegmentMemeConstellationArrangements().keySet()) {
      if (Objects.equals(segmentMemeConstellation, constellation))
        for (Arrangement arrangement : basis.previousSegmentMemeConstellationArrangements().get(constellation)) {
          if (Objects.equals(voiceId, arrangement.getVoiceId()))
            return arrangement.getInstrumentId();
        }
    }
    return null;
  }

  /**
   Pattern offset for rhythm-type sequence choice for segment
   if continues past available rhythm-type sequence patterns, loops back to beginning of sequence patterns

   @return offset of rhythm-type sequence choice
   <p>
   future: actually compute rhythm sequence pattern offset
   */
  private BigInteger rhythmPatternOffset() throws Exception {
    if (Objects.isNull(_rhythmPatternOffset))
      switch (basis.type()) {

        case Continue:
          Choice previous = basis.previousRhythmSelection();
          _rhythmPatternOffset = Objects.nonNull(previous) ? previous.nextPatternOffset() : BigInteger.ZERO;
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmPatternOffset = BigInteger.valueOf(0);
      }

    return _rhythmPatternOffset;
  }

  /**
   Transposition for rhythm-type sequence choice for segment

   @return +/- semitones transposition of rhythm-type sequence choice
   */
  private Integer rhythmTranspose() throws Exception {
    return Key.delta(rhythmSequence().getKey(), basis.segment().getKey(), 0);
  }

  /**
   Choose a fresh rhythm based on a set of memes
   FUTURE [#150279436] Key of first Pattern of chosen Rhythm-Sequence must match the `minor` or `major` with the Key of the current Segment.

   @return rhythm-type Sequence
   @throws Exception on failure
   <p>
   future: actually choose rhythm sequence
   */
  private Sequence selectFreshRhythm() throws Exception {
    EntityRank<Sequence> entityRank = new EntityRank<>();

    // (2a) retrieve sequences bound directly to chain
    Collection<Sequence> sourceSequences = basis.ingest().sequences(SequenceType.Rhythm);

    // (2b) only if none were found in the previous step, retrieve sequences bound to chain library
    if (sourceSequences.isEmpty())
      sourceSequences = basis.libraryIngest().sequences(SequenceType.Rhythm);

    // (3) score each source sequence based on meme isometry
    for (Sequence sequence : sourceSequences) {
      Double score = scoreRhythm(sequence);
      if (Objects.nonNull(score)) entityRank.add(sequence, score);
    }

    // report
    basis.report("rhythmChoice", entityRank.report());

    // (4) return the top choice
    Sequence sequence = entityRank.getTop();
    if (Objects.nonNull(sequence))
      return sequence;
    else
      throw new BusinessException("Found no rhythm-type sequence bound to Chain!");
  }

  /**
   Score a candidate for rhythm sequence, given current basis
   Score includes matching memes, previous segment to macro sequence first pattern
   <p>
   Returns ZERO if the sequence has no memes, in order to fix:
   [#162040109] Artist expects sequence with no memes will never be selected for chain craft.

   @param sequence to score
   @return score, including +/- entropy, -1 if this sequence has no memes
   */
  @Nullable
  private Double scoreRhythm(Sequence sequence) {
    try {
      Collection<Meme> memes = basis.ingest().sequenceAndPatternMemes(sequence.getId(), BigInteger.valueOf(0),
        PatternType.Intro, PatternType.Loop, PatternType.Outro);
      if (!memes.isEmpty())
        return basis.currentSegmentMemeIsometry().score(memes) * SCORE_MATCHED_MEMES + Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY);
    } catch (Exception e) {
      log.warn("While scoring rhythm {}", sequence, e);
    }
    return null;
  }

  /**
   craft segment events for all rhythm voices
   */
  private void craftRhythmVoiceArrangements() throws Exception {
    if (Objects.isNull(basis.currentRhythmChoice())) return;
    Collection<Arrangement> arrangements = Lists.newArrayList();
    for (Voice voice : voices(basis.currentRhythmChoice().getSequenceId()))
      arrangements.add(craftArrangementForRhythmVoice(voice));
    basis.setChoiceArrangements(basis.currentRhythmChoice().getId(), arrangements);
  }

  /**
   craft segment events for one rhythm voice
   [#161736024] if segment meme constellation already encountered, use that instrument-voice

   @param voice to craft events for
   @throws Exception on failure
   */
  private Arrangement craftArrangementForRhythmVoice(Voice voice) throws Exception {
    String constellation = MemeIsometry.ofMemes(basis.segmentMemes()).getConstellation();
    BigInteger instrumentId = previousVoiceInstrumentId(constellation, voice.getId());

    // if no previous instrument found, choose a fresh one
    if (Objects.isNull(instrumentId)) instrumentId = chooseFreshPercussiveInstrument(voice).getId();

    return basis.create(new Arrangement()
      .setChoiceId(basis.currentRhythmChoice().getId())
      .setVoiceId(voice.getId())
      .setInstrumentId(instrumentId));
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same sequence

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws Exception on failure
   */
  private Instrument chooseFreshPercussiveInstrument(Voice voice) throws Exception {
    EntityRank<Instrument> entityRank = new EntityRank<>();

    // (2a) retrieve instruments bound directly to chain
    Collection<Instrument> sourceInstruments = basis.ingest().instruments(InstrumentType.Percussive);

    // (2b) only if none were found in the previous transpose, retrieve instruments bound to chain library
    if (sourceInstruments.isEmpty())
      sourceInstruments = basis.libraryIngest().instruments(InstrumentType.Percussive);

    // future: [#258] Instrument selection is based on Text Isometry between the voice description and the instrument description
    log.debug("not currently in use: {}", voice);

    // (3) score each source instrument based on meme isometry
    for (Instrument instrument : sourceInstruments) {
      try {
        entityRank.add(instrument, scorePercussiveInstrument(instrument));
      } catch (Exception e) {
        log.debug("while scoring percussive instrument", e);
      }
    }

    // report
    basis.report("percussiveChoice", entityRank.report());

    // (4) return the top choice
    Instrument instrument = entityRank.getTop();
    if (Objects.nonNull(instrument))
      return instrument;
    else
      throw new BusinessException("Found no percussive-type instrument bound to Chain!");
  }

  /**
   Score a candidate for percussive instrument, given current basis

   @param instrument to score
   @return score, including +/- entropy
   */
  private double scorePercussiveInstrument(Instrument instrument) throws Exception {
    Double score = Chance.normallyAround(0, SCORE_INSTRUMENT_ENTROPY);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += basis.currentSegmentMemeIsometry().score(basis.ingest().instrumentMemes(instrument.getId())) * SCORE_MATCHED_MEMES;

    return score;
  }

  /**
   [#153976073] Artist wants Pattern to have type Macro or Main (for Macro- or Main-type sequences), or Intro, Loop, or Outro (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
   [#161466708] Artist wants dynamic randomness over the selection of various audio events to fulfill particular pattern events, in order to establish repetition within any given segment.

   @throws Exception on failure
   */
  private void craftRhythmPatterns(
  ) throws Exception {
    if (Objects.isNull(basis.currentRhythmChoice())) return;

    // choose intro pattern (if available)
    Pattern introPattern = basis.ingest().patternAtOffset(basis.currentRhythmChoice().getSequenceId(), basis.currentRhythmChoice().getSequencePatternOffset(), PatternType.Intro);

    // choose outro pattern (if available)
    Pattern outroPattern = basis.ingest().patternAtOffset(basis.currentRhythmChoice().getSequenceId(), basis.currentRhythmChoice().getSequencePatternOffset(), PatternType.Outro);

    // compute in and out points, and length # beats for which loop patterns will be required
    long loopOutPos = basis.segment().getTotal() - (Objects.nonNull(outroPattern) ? outroPattern.getTotal() : 0);

    // begin at the beginning and fabricate events for the segment from beginning to end
    double curPos = 0.0;

    // if intro pattern, fabricate those voice event first
    if (Objects.nonNull(introPattern)) {
      curPos += craftRhythmPatternPatternEvents(curPos, introPattern, loopOutPos, 0);
    }

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < loopOutPos) {
      Pattern loopPattern = basis.ingest().patternRandomAtOffset(basis.currentRhythmChoice().getSequenceId(), basis.currentRhythmChoice().getSequencePatternOffset(), PatternType.Loop);
      curPos += craftRhythmPatternPatternEvents(curPos, loopPattern, loopOutPos, 0);
    }

    // if outro pattern, fabricate those voice event last
    // [#161466708] compute how much to go for it in the outro
    if (Objects.nonNull(outroPattern)) {
      double goForItRatio = basis.currentMainChoice().getSequencePatternOffset().doubleValue() / basis.currentMainChoice().getMaxAvailablePatternOffset().doubleValue();
      craftRhythmPatternPatternEvents(curPos, outroPattern, loopOutPos, goForItRatio);
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
  private double craftRhythmPatternPatternEvents(double fromPos, Pattern pattern, double maxPos, double goForItRatio) throws Exception {
    double totalPos = maxPos - fromPos;
    Choice choice = basis.currentRhythmChoice();
    Collection<Arrangement> arrangements = basis.choiceArrangements(choice.getId());
    for (Arrangement arrangement : arrangements) {
      Collection<PatternEvent> patternEvents = basis.ingest().patternVoiceEvents(pattern.getId(), arrangement.getVoiceId());
      Instrument instrument = basis.ingest().instrument(arrangement.getInstrumentId());
      for (PatternEvent patternEvent : patternEvents) {
        double chanceOfRandomChoice = 0.0 == goForItRatio ? 0.0 : goForItRatio * Value.ratio(patternEvent.getPosition() - fromPos, totalPos);
        pickInstrumentAudio(instrument, arrangement, patternEvent, choice.getTranspose(), fromPos, chanceOfRandomChoice);
      }
    }
    return Math.min(totalPos, pattern.getTotal());
  }

  /**
   create a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement@param arrangement   to create pick within

   @param patternEvent         to pick audio for
   @param shiftPosition        offset voice event zero within current segment
   @param chanceOfRandomChoice entropy is increased during the progression of a main sequence [#161466708]
   */
  private void pickInstrumentAudio(Instrument instrument, Arrangement arrangement, PatternEvent patternEvent, int transpose, Double shiftPosition, Double chanceOfRandomChoice) throws Exception {
    Audio audio = selectInstrumentAudio(instrument, patternEvent, chanceOfRandomChoice);

    // Morph & Point attributes are expressed in beats
    double position = patternEvent.getPosition() + shiftPosition;
    double duration = patternEvent.getDuration();
    Chord chord = basis.chordAt((int) Math.floor(position));

    // The final note is transformed based on instrument type
    Note note = pickNote(
      Note.of(patternEvent.getNote()).transpose(transpose),
      chord, audio, instrument.getType());

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = basis.secondsAtPosition(position);
    double lengthSeconds = basis.secondsAtPosition(position + duration) - startSeconds;

    // create pick
    basis.pick(new Pick()
      .setArrangementId(arrangement.getId())
      .setAudioId(audio.getId())
      .setStart(startSeconds)
      .setLength(lengthSeconds)
      .setAmplitude(patternEvent.getVelocity())
      .setPitch(basis.pitch(note)));
  }

  /**
   Determine if we will use a cached or new audio for this selection
   Cached audio defaults to random selection if none has been previously encountered

   @param instrument           from which to score available audios, and make a selection
   @param patternEvent         to match
   @param chanceOfRandomChoice from 0 to 1, chance that a random audio will be selected (instead of the cached selection)
   @return matched new audio
   @throws Exception on failure
   */
  private Audio selectInstrumentAudio(Instrument instrument, PatternEvent patternEvent, Double chanceOfRandomChoice) throws Exception {
    if (0 < chanceOfRandomChoice && random.nextDouble() <= chanceOfRandomChoice) {
      return selectNewInstrumentAudio(instrument, patternEvent);
    } else {
      return selectCachedInstrumentAudio(instrument, patternEvent);
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
   @throws Exception on failure
   */
  private Audio selectCachedInstrumentAudio(Instrument instrument, PatternEvent patternEvent) throws Exception {
    String key = patternEventKey(patternEvent);
    if (!cachedSelectionInstrumentAudio.containsKey(key))
      cachedSelectionInstrumentAudio.put(key, selectNewInstrumentAudio(instrument, patternEvent));
    return cachedSelectionInstrumentAudio.get(key);
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument   from which to score available audios, and make a selection
   @param patternEvent to match
   @return matched new audio
   @throws Exception on failure
   */
  private Audio selectNewInstrumentAudio(Instrument instrument, Event patternEvent) throws Exception {
    EntityRank<Audio> audioEntityRank = new EntityRank<>();

    // add all audio to chooser
    audioEntityRank.addAll(basis.ingest().audios(instrument.getId()));

    // score each audio against the current voice event, with some variability
    for (AudioEvent audioEvent : basis.ingest().instrumentAudioFirstEvents(instrument.getId()))
      audioEntityRank.score(audioEvent.getAudioId(),
        Chance.normallyAround(
          EventIsometry.similarity(patternEvent, audioEvent),
          SCORE_INSTRUMENT_ENTROPY));

    // final chosen audio event
    Audio audio = audioEntityRank.getTop();
    if (Objects.isNull(audio))
      throw new BusinessException("No acceptable Audio found!");
    return audio;
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
    if (Objects.equals(InstrumentType.Percussive, instrumentType)) {
      return basis.note(audio.getPitch())
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
   @throws Exception on failure
   */
  private Iterable<Voice> voices(BigInteger sequenceId) throws Exception {
    List<Voice> voices = Lists.newArrayList();
    voices.addAll(basis.ingest().voices(sequenceId));
    return voices;
  }

  /**
   Report
   */
  private void report() {
    // future: basis.report() anything else interesting from the craft operation
  }

}
