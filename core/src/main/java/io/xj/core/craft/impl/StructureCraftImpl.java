// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.craft.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.work.basis.Basis;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.MemeEntity;
import io.xj.core.model.choice.Chance;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.choice.Chooser;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.tables.records.PhaseRecord;
import io.xj.core.util.Value;
import io.xj.core.craft.StructureCraft;
import io.xj.music.Key;

import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 Structure craft for the current link includes rhythm and detail
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public class StructureCraftImpl implements StructureCraft {
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_RHYTHM = 10;
  public static final double CHOOSE_RHYTHM_MAX_DISTRIBUTION = 0.5;
  private final Logger log = LoggerFactory.getLogger(StructureCraftImpl.class);
  private final Basis basis;
  private final ChoiceDAO choiceDAO;
  private final PatternDAO patternDAO;
  private Pattern _rhythmPattern;
  private ULong _rhythmPhaseOffset;

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
        .setLinkId(basis.linkId().toBigInteger())
        .setType(PatternType.Rhythm.toString())
        .setPatternId(rhythmPattern().getId().toBigInteger())
        .setTranspose(rhythmTranspose())
        .setPhaseOffset(rhythmPhaseOffset().toBigInteger()));
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
  private ULong rhythmPhaseOffset() throws Exception {
    if (Objects.isNull(_rhythmPhaseOffset))
      switch (basis.type()) {

        case Continue:
          _rhythmPhaseOffset = basis.previousRhythmChoice().nextPhaseOffset();
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _rhythmPhaseOffset = ULong.valueOf(0);
      }

    return _rhythmPhaseOffset;
  }

  /**
   Fetch current phase of rhythm-type pattern

   @return phase record
   @throws Exception on failure
   */
  private PhaseRecord rhythmPhase() throws Exception {
    PhaseRecord phase = basis.phaseByOffset(rhythmPattern().getId(), rhythmPhaseOffset());

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

    // (1) retrieve memes of macro pattern, for use as a meme isometry comparison
    MemeIsometry memeIsometry = MemeIsometry.of(basis.linkMemes());

    // (2a) retrieve patterns bound directly to chain
    Result<? extends Record> sourceRecords = patternDAO.readAllBoundToChain(Access.internal(), basis.chainId(), PatternType.Rhythm);

    // (2b) only if none were found in the previous step, retrieve patterns bound to chain library
    if (sourceRecords.isEmpty())
      sourceRecords = patternDAO.readAllBoundToChainLibrary(Access.internal(), basis.chainId(), PatternType.Rhythm);

    // (3) score each source record based on meme isometry
    sourceRecords.forEach((record -> {
      try {
        chooser.add(new Pattern().setFromRecord(record),
          Chance.normallyAround(
            memeIsometry.scoreCSV(String.valueOf(record.get(MemeEntity.KEY_MANY))),
            CHOOSE_RHYTHM_MAX_DISTRIBUTION));
      } catch (BusinessException e) {
        log.debug("While scoring records", e);
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
   Report
   */
  private void report() {
    // future: basis.report() anything else interesting from the craft operation
  }

}
