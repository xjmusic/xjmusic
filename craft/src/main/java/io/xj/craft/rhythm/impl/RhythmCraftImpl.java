// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm.impl;

import io.xj.craft.basis.Basis;
import io.xj.core.exception.BusinessException;
import io.xj.craft.isometry.EventIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.voice.Voice;
import io.xj.core.util.Chance;
import io.xj.craft.rhythm.RhythmCraft;
import io.xj.music.Chord;
import io.xj.music.Key;
import io.xj.music.Note;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 Rhythm craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class RhythmCraftImpl implements RhythmCraft {
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_RHYTHM = 10;
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private final Basis basis;
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);
  private BigInteger _rhythmPatternOffset;
  private Sequence _rhythmSequence;

  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Basis basis
  /*-*/) {
    this.basis = basis;
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
          .setPatternOffset(rhythmPatternOffset()));
  }

  /**
   compute (and cache) the mainSequence

   @return mainSequence
   */
  private Sequence rhythmSequence() throws Exception {
    if (Objects.isNull(_rhythmSequence))
      switch (basis.type()) {

        case Continue:
          Choice previousChoice = basis.previousRhythmChoice();
          if (Objects.nonNull(previousChoice))
            _rhythmSequence = basis.ingest().sequence(previousChoice.getSequenceId());
          else {
            log.warn("No rhythm-type sequence chosen in previous Segment #{}", basis.previousSegment().getId());
            _rhythmSequence = chooseRhythm();
          }

          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmSequence = chooseRhythm();
      }

    return _rhythmSequence;
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
          _rhythmPatternOffset = Objects.nonNull(basis.previousRhythmChoice()) ? basis.previousRhythmChoice().nextPatternOffset() : BigInteger.valueOf(0);
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
   Choose rhythm sequence

   @return rhythm-type Sequence
   @throws Exception on failure
   <p>
   future: actually choose rhythm sequence
   */
  private Sequence chooseRhythm() throws Exception {
    EntityRank<Sequence> entityRank = new EntityRank<>();

    // future: only choose major sequences for major keys, minor for minor! [#223] Key of first Pattern of chosen Rhythm-Sequence must match the `minor` or `major` with the Key of the current Segment.

    // (2a) retrieve sequences bound directly to chain
    Collection<Sequence> sourceSequences = basis.ingest().sequences(SequenceType.Rhythm);

    // (2b) only if none were found in the previous step, retrieve sequences bound to chain library
    if (sourceSequences.isEmpty())
      sourceSequences = basis.libraryIngest().sequences(SequenceType.Rhythm);

    // (3) score each source sequence based on meme isometry
    for (Sequence sequence : sourceSequences) {
      entityRank.add(sequence, scoreRhythm(sequence));
    }

    // (3b) Avoid previous rhythm sequence
    if (!basis.isInitialSegment())
      if (Objects.nonNull(basis.previousRhythmChoice()))
        entityRank.score(basis.previousRhythmChoice().getSequenceId(), -SCORE_AVOID_CHOOSING_PREVIOUS_RHYTHM);

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

   @param sequence to score
   @return score, including +/- entropy
   */
  private double scoreRhythm(Sequence sequence) {
    Double score = Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY);

    // Score includes matching memes, previous segment to macro sequence first pattern
    try {
      score += basis.currentSegmentMemeIsometry().score(
        basis.ingest().sequenceAndPatternMemes(sequence.getId(), BigInteger.valueOf(0),
          PatternType.Intro, PatternType.Loop, PatternType.Outro))
        * SCORE_MATCHED_MEMES;
    } catch (Exception e) {
      log.warn("While scoring rhythm {}", sequence, e);
    }

    return score;
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

   @param voice to craft events for
   @throws Exception on failure
   */
  private Arrangement craftArrangementForRhythmVoice(Voice voice) throws Exception {
    Instrument percussiveInstrument = choosePercussiveInstrument(voice);

    return basis.create(new Arrangement()
      .setChoiceId(basis.currentRhythmChoice().getId())
      .setVoiceId(voice.getId())
      .setInstrumentId(percussiveInstrument.getId()));
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same sequence

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws Exception on failure
   */
  private Instrument choosePercussiveInstrument(Voice voice) throws Exception {
    EntityRank<Instrument> entityRank = new EntityRank<>();

    // (2a) retrieve instruments bound directly to chain
    Collection<Instrument> sourceInstruments = basis.ingest().instruments(InstrumentType.Percussive);

    // (2b) only if none were found in the previous transpose, retrieve instruments bound to chain library
    if (sourceInstruments.isEmpty())
      sourceInstruments = basis.libraryIngest().instruments(InstrumentType.Percussive);

    // future: [#258] Instrument selection is based on Text Isometry between the voice description and the instrument description
    log.debug("not currently in use: {}", voice);

    // (3) score each source instrument based on meme isometry
    sourceInstruments.forEach((instrument -> {
      try {
        entityRank.add(instrument, scorePercussiveInstrument(instrument));
      } catch (Exception e) {
        log.debug("while scoring perussive instrument", e);
      }
    }));

    /*
    DISABLED for [#324] Don't take into account which instruments were previously chosen, when choosing instruments for current segment.
    // (3b) Avoid previous percussive instrument
    if (!basis.isInitialSegment())
      basis.previousPercussiveArrangements().forEach(arrangement ->
        entityRank.score(arrangement.getInstrumentId(), -SCORE_AVOID_CHOOSING_PREVIOUS));
        */

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

   @throws Exception on failure
   */
  private void craftRhythmPatterns(
  ) throws Exception {
    if (Objects.isNull(basis.currentRhythmChoice())) return;

    // choose intro pattern (if available)
    Pattern introPattern = basis.ingest().patternAtOffset(basis.currentRhythmChoice().getSequenceId(), basis.currentRhythmChoice().getPatternOffset(), PatternType.Intro);

    // choose outro pattern (if available)
    Pattern outroPattern = basis.ingest().patternAtOffset(basis.currentRhythmChoice().getSequenceId(), basis.currentRhythmChoice().getPatternOffset(), PatternType.Outro);

    // compute in and out points, and length # beats for which loop patterns will be required
    long loopOutPos = basis.segment().getTotal() - (Objects.nonNull(outroPattern) ? outroPattern.getTotal() : 0);

    // begin at the beginning and fabricate events for the segment from beginning to end
    double curPos = 0.0;

    // if intro pattern, fabricate those voice event first
    if (Objects.nonNull(introPattern)) {
      curPos += craftRhythmPatternPatternEvents(curPos, introPattern, loopOutPos);
    }

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < loopOutPos) {
      Pattern loopPattern = basis.ingest().patternRandomAtOffset(basis.currentRhythmChoice().getSequenceId(), basis.currentRhythmChoice().getPatternOffset(), PatternType.Loop);
      curPos += craftRhythmPatternPatternEvents(curPos, loopPattern, loopOutPos);
    }

    // if outro pattern, fabricate those voice event last
    if (Objects.nonNull(outroPattern)) {
      craftRhythmPatternPatternEvents(curPos, outroPattern, loopOutPos);
    }
  }

  /**
   Craft the voice events of a single rhythm pattern

   @param fromPos to write events to segment
   @param pattern   to source events
   @param maxPos  to write events to segment
   @return deltaPos from start
   */
  private double craftRhythmPatternPatternEvents(double fromPos, Pattern pattern, double maxPos) throws Exception {
    Choice choice = basis.currentRhythmChoice();
    Collection<Arrangement> arrangements = basis.choiceArrangements(choice.getId());
    for (Arrangement arrangement : arrangements) {
      Collection<PatternEvent> patternEvents = basis.ingest().patternVoiceEvents(pattern.getId(), arrangement.getVoiceId());
      Instrument instrument = basis.ingest().instrument(arrangement.getInstrumentId());
      for (PatternEvent patternEvent : patternEvents) {
        pickInstrumentAudio(instrument, arrangement, patternEvent, choice.getTranspose(), fromPos);
      }
    }
    return Math.min(maxPos - fromPos, pattern.getTotal());
  }

  /**
   create a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param arrangement   to create pick within
   @param patternEvent    to pick audio for
   @param shiftPosition offset voice event zero within current segment
   */
  private void pickInstrumentAudio(
    Instrument instrument,
    Arrangement arrangement,
    PatternEvent patternEvent,
    int transpose,
    Double shiftPosition
  /*-*/) throws Exception {
    EntityRank<Audio> audioEntityRank = new EntityRank<>();

    // add all audio to chooser
    audioEntityRank.addAll(basis.ingest().audios(instrument.getId()));

    // score each audio against the current voice event, with some variability
    basis.ingest().instrumentAudioFirstEvents(instrument.getId())
      .forEach(audioEvent ->
        audioEntityRank.score(audioEvent.getAudioId(),
          Chance.normallyAround(
            EventIsometry.similarity(patternEvent, audioEvent),
            SCORE_INSTRUMENT_ENTROPY)));

    // final chosen audio event
    Audio audio = audioEntityRank.getTop();
    if (Objects.isNull(audio))
      throw new BusinessException("No acceptable Audio found!");

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
    switch (instrumentType) {

      case Percussive:
        return basis.note(audio.getPitch())
          .conformedTo(chord);

      case Melodic:
      case Harmonic:
      case Vocal:
      default:
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
