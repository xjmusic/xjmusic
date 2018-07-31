// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.transport.CSV;
import io.xj.worker.job.SequenceEraseJob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SequenceEraseJobImpl implements SequenceEraseJob {
  private static final Logger log = LoggerFactory.getLogger(SequenceEraseJobImpl.class);
  private final BigInteger entityId;
  private final SequenceDAO sequenceDAO;
  private final PatternDAO patternDAO;

  @Inject
  public SequenceEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    SequenceDAO sequenceDAO,
    PatternDAO patternDAO
  ) {
    this.entityId = entityId;
    this.sequenceDAO = sequenceDAO;
    this.patternDAO = patternDAO;
  }

  @Override
  public void run() {
    try {
      eraseSequence();

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Sequence Erase Work
   If the Sequence is empty, Eraseworker deletes the sequence
   */
  private void eraseSequence() {
    try {

      // delete patterns
      Collection<Pattern> patterns = patternDAO.readAll(Access.internal(), ImmutableList.of(entityId));
      if (!patterns.isEmpty()) {
        List<String> patternIds = Lists.newArrayList();
        for (Pattern pattern : patterns) {
          patternIds.add(pattern.getId().toString());
        }
        log.info("Found {} patterns in sequenceId={}; patternsIds={}; attempting to erase...", patterns.size(), entityId, CSV.join(patternIds));
        erasePatterns(patterns);
      } else {
        log.info("Found ZERO patterns in sequenceId={}", entityId);
      }

      // delete sequence
      Sequence sequence = sequenceDAO.readOne(Access.internal(), entityId);
      if (Objects.nonNull(sequence)) {
        log.info("Attempting to destroy sequenceId={}", entityId);
        sequenceDAO.destroy(Access.internal(), entityId);
      } else {
        log.warn("Found NO sequenceId={}", entityId);
      }

    } catch (Exception e) {
      log.warn("Failed to delete sequenceId={}", entityId, e);
    }
  }

  /**
   Erase many patterns
   Eraseworker iterates on each pattern in the sequence, reading in batches of a limited size

   @param patterns to erase
   @throws Exception on failure
   */
  private void erasePatterns(Iterable<Pattern> patterns) throws Exception {
    for (Pattern pattern : patterns)
      erasePattern(pattern);
  }

  /**
   Erase a pattern
   Eraseworker removes all child entities for the Pattern
   Eraseworker deletes all S3 objects for the Pattern
   If the Pattern is empty and the S3 object is confirmed deleted, Eraseworker deletes the Pattern

   @param pattern to erase
   @throws Exception on failure
   */
  private void erasePattern(Pattern pattern) throws Exception {
    patternDAO.destroy(Access.internal(), pattern.getId());
    log.info("Erased Pattern #{}, destroyed child entities", pattern.getId());
  }

}
