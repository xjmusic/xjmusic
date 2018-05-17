// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.harmonic.impl;

import io.xj.craft.basis.Basis;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.sequence.Sequence;
import io.xj.craft.harmonic.HarmonicDetailCraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class HarmonicDetailCraftImpl implements HarmonicDetailCraft {
  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_DETAIL = 10;
  private static final double SCORE_DETAIL_ENTROPY = 0.5;
  private static final double SCORE_MATCHED_MEMES = 3;
  private final Logger log = LoggerFactory.getLogger(HarmonicDetailCraftImpl.class);
  private final Basis basis;
  private Sequence _detailSequence;
  private BigInteger _detailPatternOffset;

  @Inject
  public HarmonicDetailCraftImpl(
    @Assisted("basis") Basis basis
  /*-*/) {
    this.basis = basis;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      craftHarmonicDetailSequences();
      craftHarmonicDetailInstruments();
      craftHarmonicDetailPatternEvents();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type HarmonicDetailCraft for segment #%s",
          basis.type(), basis.segment().getId().toString()), e);
    }
  }

  /**
   Craft harmonic harmonicDetail sequences for segment.

   @throws Exception on failure
   */
  private void craftHarmonicDetailSequences() throws Exception {
    // TODO: determine candidate harmonic harmonic detail sequences
    // TODO: choose at least one harmonic harmonic detail sequence
    // TODO: for each harmonic choice, iterate all candidate harmonic bindings
    // TODO: for each harmonic choice, score all candidate harmonic bindings
    // TODO: for each harmonic choice, select a complete set of harmonic bindings
    // TODO: for each harmonic choice, persist harmonic bindings in memory
  }

  /**
   Craft harmonic harmonicDetail instruments for segment.

   @throws Exception on failure
   */
  private void craftHarmonicDetailInstruments() throws Exception {
    // TODO: for each harmonic choice, choose harmonic instruments
  }

  /**
   Craft harmonic harmonicDetail voice events for segment.

   @throws Exception on failure
   */
  private void craftHarmonicDetailPatternEvents() throws Exception {
    // TODO: for each harmonic choice, craft harmonic voice events
  }

  /**
   Report
   */
  private void report() {
    // future: basis.report() anything else interesting from the craft operation
  }

}
