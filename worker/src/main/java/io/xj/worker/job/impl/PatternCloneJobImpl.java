// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.voice.Voice;
import io.xj.core.transport.JSON;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.PatternCloneJob;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

public class PatternCloneJobImpl implements PatternCloneJob {
  static final Logger log = LoggerFactory.getLogger(PatternCloneJobImpl.class);
  private final BigInteger toId;
  private final PatternDAO patternDAO;
  private final BigInteger fromId;
  private final PhaseDAO phaseDAO;
  private final VoiceDAO voiceDAO;
  private final WorkManager workManager;
  private final PatternMemeDAO patternMemeDAO;

  @Inject
  public PatternCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    PatternDAO patternDAO,
    PhaseDAO phaseDAO,
    VoiceDAO voiceDAO,
    WorkManager workManager,
    PatternMemeDAO patternMemeDAO
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.patternDAO = patternDAO;
    this.phaseDAO = phaseDAO;
    this.voiceDAO = voiceDAO;
    this.workManager = workManager;
    this.patternMemeDAO = patternMemeDAO;
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
   Do Pattern Clone ExpectationOfWork
   Worker removes all child entities for the Pattern
   Worker deletes all S3 objects for the Pattern
   Worker deletes the Pattern
   */
  private void doWork() throws Exception {
    Pattern from = patternDAO.readOne(Access.internal(), fromId);
    if (Objects.isNull(from))
      throw new BusinessException("Could not fetch clone source Pattern");

    Pattern to = patternDAO.readOne(Access.internal(), toId);
    if (Objects.isNull(to))
      throw new BusinessException("Could not fetch clone target Pattern");

    // Clone PatternMeme
    patternMemeDAO.readAll(Access.internal(), fromId).forEach(patternMeme -> {
      patternMeme.setPatternId(toId);
      try {
        PatternMeme toPatternMeme = patternMemeDAO.create(Access.internal(), patternMeme);
        log.info("Cloned PatternMeme from #{} to {}", patternMeme.getId(), JSON.objectFrom(toPatternMeme));

      } catch (Exception e) {
        log.error("Failed to clone PatternMeme {}", JSON.objectFrom(patternMeme), e);
      }
    });

    // Clone each Voice and schedule an VoiceClone job
    voiceDAO.readAll(Access.internal(), fromId).forEach(fromVoice -> {
      fromVoice.setPatternId(toId);
      try {
        Voice toVoice = voiceDAO.create(Access.internal(), fromVoice);
        log.info("Cloned Voice from #{} to {}", fromVoice.getId(), JSON.objectFrom(toVoice));

      } catch (Exception e) {
        log.error("Failed to clone Voice {}", JSON.objectFrom(fromVoice), e);
      }
    });

    // Clone each Phase and schedule an PhaseClone job
    phaseDAO.readAll(Access.internal(), fromId).forEach(phase -> {
      phase.setPatternId(toId);
      try {
        Phase toPhase = phaseDAO.create(Access.internal(), phase);
        workManager.schedulePhaseClone(0, phase.getId(), toPhase.getId());
        log.info("Cloned Phase from #{} to {} and scheduled PhaseClone job", phase.getId(), JSON.objectFrom(toPhase));

      } catch (Exception e) {
        log.error("Failed to clone Phase {}", JSON.objectFrom(phase), e);
      }
    });

    log.info("Cloned Pattern #{} and child entities to new Pattern {}", fromId, JSON.objectFrom(to));
  }


}
