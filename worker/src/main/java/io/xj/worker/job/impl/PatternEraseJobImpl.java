// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.craft.exception.CraftException;
import io.xj.worker.job.PatternEraseJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class PatternEraseJobImpl implements PatternEraseJob {
  static final Logger log = LoggerFactory.getLogger(PatternEraseJobImpl.class);
  private final PatternDAO patternDAO;
  private final BigInteger entityId;
  private final Access access = Access.internal();

  @Inject
  public PatternEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    PatternDAO patternDAO
  ) {
    this.entityId = entityId;
    this.patternDAO = patternDAO;
  }

  @Override
  public void run() {
    try {
      Pattern pattern = patternDAO.readOne(access, entityId);
      doWork(pattern);

    } catch (CraftException e) {
      log.warn("Did not erase patternId={}, reason={}", entityId, e.getMessage());

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Pattern Erase Work
   Eraseworker removes all child entities for the Pattern
   Eraseworker deletes all S3 objects for the Pattern
   Eraseworker deletes the Pattern

   @param pattern to work on
   */
  private void doWork(Pattern pattern) throws Exception {
    patternDAO.destroy(access, pattern.getId());
    log.info("Erased Pattern #{}, destroyed child entities", pattern.getId());
  }

}
