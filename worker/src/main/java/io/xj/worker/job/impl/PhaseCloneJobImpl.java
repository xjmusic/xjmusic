// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.transport.JSON;
import io.xj.worker.job.PhaseCloneJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class PhaseCloneJobImpl implements PhaseCloneJob {
  static final Logger log = LoggerFactory.getLogger(PhaseCloneJobImpl.class);
  private final BigInteger toId;
  private final PhaseDAO phaseDAO;
  private final BigInteger fromId;
  private final PhaseMemeDAO phaseMemeDAO;
  private final PhaseChordDAO phaseChordDAO;
  private final VoiceEventDAO voiceEventDAO;

  @Inject
  public PhaseCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO,
    PhaseChordDAO phaseChordDAO,
    VoiceEventDAO voiceEventDAO
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
    this.phaseChordDAO = phaseChordDAO;
    this.voiceEventDAO = voiceEventDAO;
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
   Do Phase Clone ExpectationOfWork
   Worker removes all child entities for the Phase
   Worker deletes all S3 objects for the Phase
   Worker deletes the Phase
   */
  private void doWork() throws Exception {
    Phase from = phaseDAO.readOne(Access.internal(), fromId);
    if (Objects.isNull(from))
      throw new BusinessException("Could not fetch clone source Phase");

    Phase to = phaseDAO.readOne(Access.internal(), toId);
    if (Objects.isNull(to))
      throw new BusinessException("Could not fetch clone target Phase");

    // Clone PhaseMeme
    phaseMemeDAO.readAll(Access.internal(), fromId).forEach(phaseMeme -> {
      phaseMeme.setPhaseId(toId);

      try {
        PhaseMeme toPhaseMeme = phaseMemeDAO.create(Access.internal(), phaseMeme);
        log.info("Cloned PhaseMeme from #{} to {}", phaseMeme.getId(), JSON.objectFrom(toPhaseMeme));

      } catch (Exception e) {
        log.error("Failed to clone PhaseMeme {}", JSON.objectFrom(phaseMeme), e);
      }
    });

    // Clone PhaseChord
    phaseChordDAO.readAll(Access.internal(), fromId).forEach(phaseChord -> {
      phaseChord.setPhaseId(toId);

      try {
        PhaseChord toPhaseChord = phaseChordDAO.create(Access.internal(), phaseChord);
        log.info("Cloned PhaseChord from #{} to {}", phaseChord.getId(), JSON.objectFrom(toPhaseChord));

      } catch (Exception e) {
        log.error("Failed to clone PhaseChord {}", JSON.objectFrom(phaseChord), e);
      }
    });

    //  Clone each VoiceEvent
    voiceEventDAO.readAll(Access.internal(), fromId).forEach(voiceEvent -> {
      voiceEvent.setPhaseId(toId);

      try {
        VoiceEvent toVoiceEvent = voiceEventDAO.create(Access.internal(), voiceEvent);
        log.info("Cloned VoiceEvent from #{} to {}", voiceEvent.getId(), JSON.objectFrom(toVoiceEvent));

      } catch (Exception e) {
        log.error("Failed to clone VoiceEvent from {}", JSON.objectFrom(voiceEvent), e);
      }
    });

    log.info("Cloned Phase #{} and child entities to new Phase {}", fromId, JSON.objectFrom(to));
  }


}
