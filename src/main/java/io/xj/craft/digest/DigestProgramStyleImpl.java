// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.entity.ChordEntity;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 In-memory cache of ingest of all memes in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@SuppressWarnings("UnstableApiUsage")
public class DigestProgramStyleImpl extends DigestImpl implements DigestProgramStyle {
  private final Logger log = LoggerFactory.getLogger(DigestProgramStyleImpl.class);
  private final Multiset<Double> mainChordSpacingHistogram = ConcurrentHashMultiset.create();
  private final Multiset<Integer> mainSequencesPerSequenceHistogram = ConcurrentHashMultiset.create();
  private final Multiset<Integer> mainSequenceTotalHistogram = ConcurrentHashMultiset.create();
  private final StatsAccumulator mainSequencesPerSequenceStats = new StatsAccumulator();
  private final StatsAccumulator mainSequenceTotalStats = new StatsAccumulator();

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestProgramStyleImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestSequenceStyle);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest sequence style create ingest {}", ingest, e);
    }
  }

  /**
   Digest entities of ingest
   */
  private void digest() {
    for (Program program : ingest.getProgramsOfType(ProgramType.Main)) {
      Collection<ProgramSequence> sequences = ingest.getSequences(program);
      mainSequencesPerSequenceStats.add(sequences.size());
      mainSequencesPerSequenceHistogram.add(sequences.size());
      for (ProgramSequence sequence : sequences) {
        Integer total = sequence.getTotal();
        mainSequenceTotalStats.add(total);
        mainSequenceTotalHistogram.add(total);

        List<ProgramSequenceChord> chords = Lists.newArrayList(ingest.getChords(sequence));
        chords.sort(ChordEntity.byPositionAscending);
        double cursor = 0;
        for (ChordEntity chord : chords)
          if (chord.getPosition() > cursor) {
            mainChordSpacingHistogram.add(chord.getPosition() - cursor);
            cursor = chord.getPosition();
          }
      }
    }
    log.debug("Digested style create {} main-type sequences containing {} sequences.", mainSequencesPerSequenceStats.count(), mainSequenceTotalStats.count());
  }

  public Multiset<Integer> getMainSequencesPerProgramHistogram() {
    return mainSequencesPerSequenceHistogram;
  }

  public StatsAccumulator getMainSequencesPerProgramStats() {
    return mainSequencesPerSequenceStats;
  }

  public StatsAccumulator getMainSequenceTotalStats() {
    return mainSequenceTotalStats;
  }

  public Multiset<Integer> getMainSequenceTotalHistogram() {
    return mainSequenceTotalHistogram;
  }

  @Override
  public Multiset<Double> getMainChordSpacingHistogram() {
    return mainChordSpacingHistogram;
  }
}
