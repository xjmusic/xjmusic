// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.model.phase.Phase;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.PhaseEraseJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class PhaseEraseJobImpl implements PhaseEraseJob {
  static final Logger log = LoggerFactory.getLogger(PhaseEraseJobImpl.class);
  private final PhaseDAO phaseDAO;
  private final BigInteger entityId;
  private final WorkManager workManager;

  @Inject
  public PhaseEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    PhaseDAO phaseDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.phaseDAO = phaseDAO;
    this.workManager = workManager;
  }


  @Override
  public void run() {
    try {
      Phase phase = phaseDAO.readOne(Access.internal(), entityId);
      if (Objects.nonNull(phase)) {
        erase(phase);
      }

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Phase Erase ExpectationOfWork
   Eraseworker removes all child entities for the Phase
   Eraseworker deletes all S3 objects for the Phase
   Eraseworker deletes the Phase
   */
  private void erase(Phase phase) throws Exception {
    phaseDAO.destroy(Access.internal(), phase.getId());
    log.info("Erased Phase #{}, destroyed child entities", phase.getId());
  }

}
