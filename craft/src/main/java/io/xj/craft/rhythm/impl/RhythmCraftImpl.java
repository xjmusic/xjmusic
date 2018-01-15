// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.isometry.EventIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.choice.Chance;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.choice.Chooser;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.work.basis.Basis;
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
 Rhythm craft for the current link
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public class RhythmCraftImpl implements RhythmCraft {
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_RHYTHM = 10;
  private static final double SCORE_INSTRUMENT_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 5;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private final ArrangementDAO arrangementDAO;
  private final Basis basis;
  private final ChoiceDAO choiceDAO;
  private final InstrumentDAO instrumentDAO;
  private final Logger log = LoggerFactory.getLogger(RhythmCraftImpl.class);
  private final PatternDAO patternDAO;
  private BigInteger _rhythmPhaseOffset;
  private Pattern _rhythmPattern;

  @Inject
  public RhythmCraftImpl(
    @Assisted("basis") Basis basis,
    ArrangementDAO arrangementDAO,
    ChoiceDAO choiceDAO,
    InstrumentDAO instrumentDAO,
    PatternDAO patternDAO
  /*-*/) {
    this.arrangementDAO = arrangementDAO;
    this.basis = basis;
    this.choiceDAO = choiceDAO;
    this.instrumentDAO = instrumentDAO;
    this.patternDAO = patternDAO;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      craftRhythm();
      craftRhythmVoiceArrangements();
      craftRhythmPhases();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type RhythmCraft for link #%s",
          basis.type(), basis.link().getId().toString()), e);
    }
  }

  /**
   craft link rhythm
   */
  private void craftRhythm() throws Exception {
    Pattern pattern = rhythmPattern();
    if (Objects.nonNull(pattern))
      choiceDAO.create(Access.internal(),
        new Choice()
          .setLinkId(basis.link().getId())
          .setType(PatternType.Rhythm.toString())
          .setPatternId(pattern.getId())
          .setTranspose(rhythmTranspose())
          .setPhaseOffset(rhythmPhaseOffset()));
  }

  /**
   compute (and cache) the mainPattern

   @return mainPattern
   */
  private Pattern rhythmPattern() throws Exception {
    if (Objects.isNull(_rhythmPattern))
      switch (basis.type()) {

        case Continue:
          Choice previousChoice = basis.previousRhythmChoice();
          if (Objects.nonNull(previousChoice))
            _rhythmPattern = basis.pattern(previousChoice.getPatternId());
          else {
            log.warn("No rhythm-type pattern chosen in previous Link #{}", basis.previousLink().getId());
            _rhythmPattern = chooseRhythm();
          }

          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmPattern = chooseRhythm();
      }

    return _rhythmPattern;
  }

  /**
   Phase offset for rhythm-type pattern choice for link
   if continues past available rhythm-type pattern phases, loops back to beginning of pattern phases

   @return offset of rhythm-type pattern choice
   <p>
   future: actually compute rhythm pattern phase offset
   */
  private BigInteger rhythmPhaseOffset() throws Exception {
    if (Objects.isNull(_rhythmPhaseOffset))
      switch (basis.type()) {

        case Continue:
          _rhythmPhaseOffset = Objects.nonNull(basis.previousRhythmChoice()) ? basis.previousRhythmChoice().nextPhaseOffset() : BigInteger.valueOf(0);
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmPhaseOffset = BigInteger.valueOf(0);
      }

    return _rhythmPhaseOffset;
  }

  /**
   Transposition for rhythm-type pattern choice for link

   @return +/- semitones transposition of rhythm-type pattern choice
   */
  private Integer rhythmTranspose() throws Exception {
    return Key.delta(rhythmPattern().getKey(), basis.link().getKey(), 0);
  }

  /**
   Choose rhythm pattern

   @return rhythm-type Pattern
   @throws Exception on failure
   <p>
   future: actually choose rhythm pattern
   */
  private Pattern chooseRhythm() throws Exception {
    Chooser<Pattern> chooser = new Chooser<>();

    // future: only choose major patterns for major keys, minor for minor! [#223] Key of first Phase of chosen Rhythm-Pattern must match the `minor` or `major` with the Key of the current Link.

    // (2a) retrieve patterns bound directly to chain
    Collection<Pattern> sourcePatterns = patternDAO.readAllBoundToChain(Access.internal(), basis.chainId(), PatternType.Rhythm);

    // (2b) only if none were found in the previous step, retrieve patterns bound to chain library
    if (sourcePatterns.isEmpty())
      sourcePatterns = patternDAO.readAllBoundToChainLibrary(Access.internal(), basis.chainId(), PatternType.Rhythm);

    // (3) score each source pattern based on meme isometry
    for (Pattern pattern : sourcePatterns) {
      chooser.add(pattern, scoreRhythm(pattern));
    }

    // (3b) Avoid previous rhythm pattern
    if (!basis.isInitialLink())
      if (Objects.nonNull(basis.previousRhythmChoice()))
        chooser.score(basis.previousRhythmChoice().getPatternId(), -SCORE_AVOID_CHOOSING_PREVIOUS_RHYTHM);

    // report
    basis.report("rhythmChoice", chooser.report());

    // (4) return the top choice
    Pattern pattern = chooser.getTop();
    if (Objects.nonNull(pattern))
      return pattern;
    else
      throw new BusinessException("Found no rhythm-type pattern bound to Chain!");
  }

  /**
   Score a candidate for rhythm pattern, given current basis

   @param pattern to score
   @return score, including +/- entropy
   */
  private double scoreRhythm(Pattern pattern) {
    Double score = Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY);

    // Score includes matching memes, previous link to macro pattern first phase
    try {
      score += basis.currentLinkMemeIsometry().score(
        basis.patternAndPhaseMemes(pattern.getId(), BigInteger.valueOf(0),
          PhaseType.Intro, PhaseType.Loop, PhaseType.Outro))
        * SCORE_MATCHED_MEMES;
    } catch (Exception e) {
      log.warn("While scoring rhythm {}", pattern, e);
    }

    return score;
  }

  /**
   craft link events for all rhythm voices
   */
  private void craftRhythmVoiceArrangements() throws Exception {
    if (Objects.isNull(basis.currentRhythmChoice())) return;
    Collection<Arrangement> arrangements = Lists.newArrayList();
    for (Voice voice : voices(basis.currentRhythmChoice().getPatternId()))
      arrangements.add(craftArrangementForRhythmVoice(voice));
    basis.setChoiceArrangements(basis.currentRhythmChoice().getId(), arrangements);
  }

  /**
   craft link events for one rhythm voice

   @param voice to craft events for
   @throws Exception on failure
   */
  private Arrangement craftArrangementForRhythmVoice(Voice voice) throws Exception {
    Instrument percussiveInstrument = choosePercussiveInstrument(voice);

    return arrangementDAO.create(Access.internal(),
      new Arrangement()
        .setChoiceId(basis.currentRhythmChoice().getId())
        .setVoiceId(voice.getId())
        .setInstrumentId(percussiveInstrument.getId()));
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same pattern

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws Exception on failure
   */
  private Instrument choosePercussiveInstrument(Voice voice) throws Exception {
    Chooser<Instrument> chooser = new Chooser<>();

    // (2a) retrieve instruments bound directly to chain
    Collection<Instrument> sourceInstruments = instrumentDAO.readAllBoundToChain(Access.internal(), basis.chainId(), InstrumentType.Percussive);

    // (2b) only if none were found in the previous transpose, retrieve instruments bound to chain library
    if (sourceInstruments.isEmpty())
      sourceInstruments = instrumentDAO.readAllBoundToChainLibrary(Access.internal(), basis.chainId(), InstrumentType.Percussive);

    // future: [#258] Instrument selection is based on Text Isometry between the voice description and the instrument description
    log.debug("not currently in use: {}", voice);

    // (3) score each source instrument based on meme isometry
    sourceInstruments.forEach((instrument -> {
      try {
        chooser.add(instrument, scorePercussiveInstrument(instrument));
      } catch (Exception e) {
        log.debug("while scoring perussive instrument", e);
      }
    }));

    /*
    DISABLED for [#324] Don't take into account which instruments were previously chosen, when choosing instruments for current link.
    // (3b) Avoid previous percussive instrument
    if (!basis.isInitialLink())
      basis.previousPercussiveArrangements().forEach(arrangement ->
        chooser.score(arrangement.getInstrumentId(), -SCORE_AVOID_CHOOSING_PREVIOUS));
        */

    // report
    basis.report("percussiveChoice", chooser.report());

    // (4) return the top choice
    Instrument instrument = chooser.getTop();
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

    // Score includes matching memes, previous link to macro instrument first phase
    score += basis.currentLinkMemeIsometry().score(basis.instrumentMemes(instrument.getId())) * SCORE_MATCHED_MEMES;

    return score;
  }

  /**
   [#153976073] Artist wants Phase to have type Macro or Main (for Macro- or Main-type patterns), or Intro, Loop, or Outro (for Rhythm or Detail-type Pattern) in order to create a composition that is dynamic when chosen to fill a Link.

   @throws Exception on failure
   */
  private void craftRhythmPhases(
  ) throws Exception {
    if (Objects.isNull(basis.currentRhythmChoice())) return;

    // choose intro phase (if available)
    Phase introPhase = basis.phaseAtOffset(basis.currentRhythmChoice().getPatternId(), basis.currentRhythmChoice().getPhaseOffset(), PhaseType.Intro);

    // choose outro phase (if available)
    Phase outroPhase = basis.phaseAtOffset(basis.currentRhythmChoice().getPatternId(), basis.currentRhythmChoice().getPhaseOffset(), PhaseType.Outro);

    // compute in and out points, and length # beats for which loop phases will be required
    long loopOutPos = basis.link().getTotal() - (Objects.nonNull(outroPhase) ? outroPhase.getTotal() : 0);

    // begin at the beginning and fabricate events for the link from beginning to end
    double curPos = 0.0;

    // if intro phase, fabricate those voice event first
    if (Objects.nonNull(introPhase)) {
      curPos += craftRhythmPhasePhaseEvents(curPos, introPhase, loopOutPos);
    }

    // choose loop phases until arrive at the out point or end of link
    while (curPos < loopOutPos) {
      Phase loopPhase = basis.phaseRandomAtOffset(basis.currentRhythmChoice().getPatternId(), basis.currentRhythmChoice().getPhaseOffset(), PhaseType.Loop);
      curPos += craftRhythmPhasePhaseEvents(curPos, loopPhase, loopOutPos);
    }

    // if outro phase, fabricate those voice event last
    if (Objects.nonNull(outroPhase)) {
      craftRhythmPhasePhaseEvents(curPos, outroPhase, loopOutPos);
    }
  }

  /**
   Craft the voice events of a single rhythm phase

   @param fromPos to write events to link
   @param phase   to source events
   @param maxPos  to write events to link
   @return deltaPos from start
   */
  private double craftRhythmPhasePhaseEvents(double fromPos, Phase phase, double maxPos) throws Exception {
    Choice choice = basis.currentRhythmChoice();
    Collection<Arrangement> arrangements = basis.choiceArrangements(choice.getId());
    for (Arrangement arrangement : arrangements) {
      Collection<PhaseEvent> phaseEvents = basis.phasePhaseEvents(phase.getId(), arrangement.getVoiceId());
      Instrument instrument = basis.instrument(arrangement.getInstrumentId());
      for (PhaseEvent phaseEvent : phaseEvents) {
        pickInstrumentAudio(instrument, arrangement, phaseEvent, choice.getTranspose(), fromPos);
      }
    }
    return Math.min(maxPos - fromPos, phase.getTotal());
  }

  /**
   create a pick of instrument-audio for each event, where events are conformed to chords/scales based on the master link chords
   pick instrument audio for one event, in a voice in a phase, belonging to an arrangement

   @param arrangement   to create pick within
   @param phaseEvent    to pick audio for
   @param shiftPosition offset voice event zero within current link
   */
  private void pickInstrumentAudio(
    Instrument instrument,
    Arrangement arrangement,
    PhaseEvent phaseEvent,
    int transpose,
    Double shiftPosition
  /*-*/) throws Exception {
    Chooser<Audio> audioChooser = new Chooser<>();

    // add all audio to chooser
    audioChooser.addAll(basis.instrumentAudios(instrument.getId()));

    // score each audio against the current voice event, with some variability
    basis.instrumentAudioEvents(instrument.getId())
      .forEach(audioEvent ->
        audioChooser.score(audioEvent.getAudioId(),
          Chance.normallyAround(
            EventIsometry.similarity(phaseEvent, audioEvent),
            SCORE_INSTRUMENT_ENTROPY)));

    // final chosen audio event
    Audio audio = audioChooser.getTop();
    if (Objects.isNull(audio))
      throw new BusinessException("No acceptable Audio found!");

    // Morph & Point attributes are expressed in beats
    double position = phaseEvent.getPosition() + shiftPosition;
    double duration = phaseEvent.getDuration();
    Chord chord = basis.chordAt((int) Math.floor(position));

    // The final note is transformed based on instrument type
    Note note = pickNote(
      Note.of(phaseEvent.getNote()).transpose(transpose),
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
      .setAmplitude(phaseEvent.getVelocity())
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
   all voices in current phase of chosen rhythm-type pattern

   @param patternId to get voices of
   @return voices for pattern
   @throws Exception on failure
   */
  private Iterable<Voice> voices(BigInteger patternId) throws Exception {
    List<Voice> voices = Lists.newArrayList();
    voices.addAll(basis.voices(patternId));
    return voices;
  }

  /**
   Report
   */
  private void report() {
    // future: basis.report() anything else interesting from the craft operation
  }

}
