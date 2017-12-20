// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.access.impl.Access;
import io.xj.craft.StructureCraft;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.choice.Chance;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.choice.Chooser;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.util.Value;
import io.xj.core.work.basis.Basis;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;

/**
 Structure craft for the current link includes rhythm and detail
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public class StructureCraftImpl implements StructureCraft {
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_RHYTHM = 10;
  private static final double SCORE_RHYTHM_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 3;
  private final Logger log = LoggerFactory.getLogger(StructureCraftImpl.class);
  private final Basis basis;
  private final ChoiceDAO choiceDAO;
  private final PatternDAO patternDAO;
  private Pattern _rhythmPattern;
  private BigInteger _rhythmPhaseOffset;

  @Inject
  public StructureCraftImpl(
    @Assisted("basis") Basis basis,
    ChoiceDAO choiceDAO,
    PatternDAO patternDAO
  /*-*/) {
    this.basis = basis;
    this.choiceDAO = choiceDAO;
    this.patternDAO = patternDAO;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      craftRhythm();
      craftDetail();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type StructureCraft for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }

  /**
   craft link rhythm
   */
  private void craftRhythm() throws Exception {
    choiceDAO.create(Access.internal(),
      new Choice()
        .setLinkId(basis.linkId())
        .setType(PatternType.Rhythm.toString())
        .setPatternId(rhythmPattern().getId())
        .setTranspose(rhythmTranspose())
        .setPhaseOffset(rhythmPhaseOffset()));
  }

  /**
   craft link detail
   */
  private void craftDetail() throws Exception {
    // future: craft detail
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
          if (Objects.isNull(previousChoice))
            throw new BusinessException("No rhythm-type pattern chosen in previous link!");
          _rhythmPattern = basis.pattern(previousChoice.getPatternId());
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
          _rhythmPhaseOffset = basis.previousRhythmChoice().nextPhaseOffset();
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmPhaseOffset = BigInteger.valueOf(0);
      }

    return _rhythmPhaseOffset;
  }

  /**
   Fetch current phase of rhythm-type pattern

   @return phase
   @throws Exception on failure
   */
  private Phase rhythmPhase() throws Exception {
    Phase phase = basis.phaseByOffset(rhythmPattern().getId(), rhythmPhaseOffset());

    if (Objects.isNull(phase))
      throw new BusinessException("rhythm-phase does not exist!");

    return phase;
  }

  /**
   Transposition for rhythm-type pattern choice for link

   @return +/- semitones transposition of rhythm-type pattern choice
   */
  private Integer rhythmTranspose() throws Exception {
    return Key.delta(
      Value.eitherOr(rhythmPhase().getKey(), rhythmPattern().getKey())
      , basis.link().getKey(), 0);
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
    sourcePatterns.forEach((pattern -> {
      try {
        chooser.add(pattern, scoreRhythm(pattern));
      } catch (Exception e) {
        log.debug("While scoring rhythm patterns", e);
      }
    }));

    // (3b) Avoid previous rhythm pattern
    if (!basis.isInitialLink())
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
   @throws Exception on failure
   */
  private double scoreRhythm(Pattern pattern) throws Exception {
    Double score = Chance.normallyAround(0, SCORE_RHYTHM_ENTROPY);

    // Score includes matching memes, previous link to macro pattern first phase
    score += basis.currentLinkMemeIsometry().score(basis.patternPhaseMemes(pattern.getId(), BigInteger.valueOf(0))) * SCORE_MATCHED_MEMES;

    return score;
  }

  /**
   Report
   */
  private void report() {
    // future: basis.report() anything else interesting from the craft operation
  }

}
