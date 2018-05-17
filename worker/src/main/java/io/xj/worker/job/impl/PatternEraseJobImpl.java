// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.PatternEraseJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class PatternEraseJobImpl implements PatternEraseJob {
  static final Logger log = LoggerFactory.getLogger(PatternEraseJobImpl.class);
  private final PatternDAO patternDAO;
  private final BigInteger entityId;
  private final WorkManager workManager;

  @Inject
  public PatternEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    PatternDAO patternDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.patternDAO = patternDAO;
    this.workManager = workManager;
  }


  @Override
  public void run() {
    try {
      Pattern pattern = patternDAO.readOne(Access.internal(), entityId);
      if (Objects.nonNull(pattern)) {
        log.info("Attempting to destroy patternId={}", entityId);
        patternDAO.destroy(Access.internal(), entityId);
      } else {
        log.info("Found NO patternId={}", entityId);
      }

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Pattern Erase ExpectationOfWork
   Eraseworker removes all child entities for the Pattern
   Eraseworker deletes all S3 objects for the Pattern
   Eraseworker deletes the Pattern
   */
  private void erase(Pattern pattern) throws Exception {
    patternDAO.destroy(Access.internal(), pattern.getId());
    log.info("Erased Pattern #{}, destroyed child entities", pattern.getId());
  }

}
