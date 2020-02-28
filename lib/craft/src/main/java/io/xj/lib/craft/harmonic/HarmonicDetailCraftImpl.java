// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.harmonic;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.fabricator.Fabricator;
import io.xj.lib.core.fabricator.FabricatorType;
import io.xj.lib.craft.exception.CraftException;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class HarmonicDetailCraftImpl implements HarmonicDetailCraft {
  //  private static final double SCORE_AVOID_CHOOSING_PREVIOUS_DETAIL = 10;
//  private static final double SCORE_DETAIL_ENTROPY = 0.5;
//  private static final double SCORE_MATCHED_MEMES = 3;
//  private final Logger log = LoggerFactory.getLogger(HarmonicDetailCraftImpl.class);
  private final Fabricator fabricator;
//  private Sequence _detailSequence;
//  private BigInteger _detailPatternOffset;

  @Inject
  public HarmonicDetailCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  @Override
  public void doWork() throws CraftException {
    try {
      FabricatorType type = fabricator.getType();
      craftHarmonicDetailSequences();
      craftHarmonicDetailInstruments();
      craftHarmonicDetailPatternEvents();
      report();

    } catch (CoreException e) {
      throw new CraftException(
        String.format("Failed to do %s-type HarmonicDetailCraft for segment #%s",
          fabricator.getSegment().getType(), fabricator.getSegment().getId().toString()), e);
    }
  }

  /**
   Craft harmonic harmonicDetail sequences for segment.
   */
  private void craftHarmonicDetailSequences() {
    // FUTURE: determine candidate harmonic harmonic detail sequences
    // FUTURE: choose at least one harmonic harmonic detail sequence
    // FUTURE: for each harmonic choice, iterate all candidate harmonic bindings
    // FUTURE: for each harmonic choice, score all candidate harmonic bindings
    // FUTURE: for each harmonic choice, select a complete set of harmonic bindings
    // FUTURE: for each harmonic choice, persist harmonic bindings in memory
  }

  /**
   Craft harmonic harmonicDetail instruments for segment.
   */
  private void craftHarmonicDetailInstruments() {
    // FUTURE: for each harmonic choice, choose harmonic instruments
  }

  /**
   Craft harmonic harmonicDetail voice events for segment.
   */
  private void craftHarmonicDetailPatternEvents() {
    // FUTURE: for each harmonic choice, craft harmonic voice events
  }

  /**
   Report
   */
  private void report() {
    // future: fabricator.report() anything else interesting of the craft operation
  }

}
