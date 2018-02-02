// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.Phase;
import io.xj.core.transport.CSV;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.PatternEraseJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

public class PatternEraseJobImpl implements PatternEraseJob {
  private static final Logger log = LoggerFactory.getLogger(PatternEraseJobImpl.class);
  private final BigInteger entityId;
  private final PatternDAO patternDAO;
  private final PhaseDAO phaseDAO;
  private final WorkManager workManager;

  @Inject
  public PatternEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    PatternDAO patternDAO,
    PhaseDAO phaseDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.patternDAO = patternDAO;
    this.phaseDAO = phaseDAO;
    this.workManager = workManager;
  }

  @Override
  public void run() {
    try {
      erasePattern();

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Pattern Erase Work
   If the Pattern is empty, Eraseworker deletes the pattern

   @throws Exception on failure
   */
  private void erasePattern() throws Exception {
    Collection<Phase> phases = phaseDAO.readAll(Access.internal(), ImmutableList.of(entityId));
    if (phases.isEmpty())
      try {
        log.info("Found ZERO phases in patternId={}; attempting to delete...", entityId);
        patternDAO.destroy(Access.internal(), entityId);
      } catch (Exception e) {
        log.warn("Failed to delete patternId={}", entityId, e);
      }
    else {
      List<String> phaseIds = Lists.newArrayList();
      for (Phase phase : phases) {
        phaseIds.add(phase.getId().toString());
      }
      log.info("Found {} phases in patternId={}; phasesIds={}; attempting to erase...", phases.size(), entityId, CSV.join(phaseIds));
      erasePhases(phases);
    }
  }

  /**
   Erase many phases
   Eraseworker iterates on each phase in the pattern, reading in batches of a limited size

   @param phases to erase
   @throws Exception on failure
   */
  private void erasePhases(Iterable<Phase> phases) throws Exception {
    for (Phase phase : phases)
      erasePhase(phase);
  }

  /**
   Erase a phase
   Eraseworker removes all child entities for the Phase
   Eraseworker deletes all S3 objects for the Phase
   If the Phase is empty and the S3 object is confirmed deleted, Eraseworker deletes the Phase

   @param phase to erase
   @throws Exception on failure
   */
  private void erasePhase(Phase phase) throws Exception {
    phaseDAO.destroy(Access.internal(), phase.getId());
    log.info("Erased Phase #{}, destroyed child entities", phase.getId());
  }

}
