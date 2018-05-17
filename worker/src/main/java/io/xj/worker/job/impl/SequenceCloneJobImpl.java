// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.voice.Voice;
import io.xj.core.transport.JSON;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.SequenceCloneJob;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class SequenceCloneJobImpl implements SequenceCloneJob {
  static final Logger log = LoggerFactory.getLogger(SequenceCloneJobImpl.class);
  private final BigInteger toId;
  private final SequenceDAO sequenceDAO;
  private final BigInteger fromId;
  private final PatternDAO patternDAO;
  private final VoiceDAO voiceDAO;
  private final WorkManager workManager;
  private final SequenceMemeDAO sequenceMemeDAO;

  @Inject
  public SequenceCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    SequenceDAO sequenceDAO,
    PatternDAO patternDAO,
    VoiceDAO voiceDAO,
    WorkManager workManager,
    SequenceMemeDAO sequenceMemeDAO
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.sequenceDAO = sequenceDAO;
    this.patternDAO = patternDAO;
    this.voiceDAO = voiceDAO;
    this.workManager = workManager;
    this.sequenceMemeDAO = sequenceMemeDAO;
  }

  @Override
  public void run() {
    try {
      if (Objects.nonNull(fromId) && Objects.nonNull(toId)) {
        doWork();
      }

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Sequence Clone ExpectationOfWork
   Worker removes all child entities for the Sequence
   Worker deletes all S3 objects for the Sequence
   Worker deletes the Sequence
   */
  private void doWork() throws Exception {
    Sequence from = sequenceDAO.readOne(Access.internal(), fromId);
    if (Objects.isNull(from))
      throw new BusinessException("Could not fetch clone source Sequence");

    Sequence to = sequenceDAO.readOne(Access.internal(), toId);
    if (Objects.isNull(to))
      throw new BusinessException("Could not fetch clone target Sequence");

    // Clone SequenceMeme
    sequenceMemeDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(sequenceMeme -> {
      sequenceMeme.setSequenceId(toId);
      try {
        SequenceMeme toSequenceMeme = sequenceMemeDAO.create(Access.internal(), sequenceMeme);
        log.info("Cloned SequenceMeme from #{} to {}", sequenceMeme.getId(), JSON.objectFrom(toSequenceMeme));

      } catch (Exception e) {
        log.error("Failed to clone SequenceMeme {}", JSON.objectFrom(sequenceMeme), e);
      }
    });

    // Clone each Voice and schedule an VoiceClone job
    voiceDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(fromVoice -> {
      fromVoice.setSequenceId(toId);
      try {
        Voice toVoice = voiceDAO.create(Access.internal(), fromVoice);
        log.info("Cloned Voice from #{} to {}", fromVoice.getId(), JSON.objectFrom(toVoice));

      } catch (Exception e) {
        log.error("Failed to clone Voice {}", JSON.objectFrom(fromVoice), e);
      }
    });

    // Clone each Pattern and schedule an PatternClone job
    patternDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(pattern -> {
      pattern.setSequenceId(toId);
      try {
        Pattern toPattern = patternDAO.create(Access.internal(), pattern);
        workManager.doPatternClone(pattern.getId(), toPattern.getId());
        log.info("Cloned Pattern from #{} to {} and scheduled PatternClone job", pattern.getId(), JSON.objectFrom(toPattern));

      } catch (Exception e) {
        log.error("Failed to clone Pattern {}", JSON.objectFrom(pattern), e);
      }
    });

    log.info("Cloned Sequence #{} and child entities to new Sequence {}", fromId, JSON.objectFrom(to));
  }


}
