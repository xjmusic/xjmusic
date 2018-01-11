// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.transport.JSON;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.InstrumentCloneJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class InstrumentCloneJobImpl implements InstrumentCloneJob {
  static final Logger log = LoggerFactory.getLogger(InstrumentCloneJobImpl.class);
  private final BigInteger toId;
  private final InstrumentDAO instrumentDAO;
  private final BigInteger fromId;
  private final AudioDAO audioDAO;
  private final WorkManager workManager;
  private final InstrumentMemeDAO instrumentMemeDAO;

  @Inject
  public InstrumentCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    InstrumentDAO instrumentDAO,
    AudioDAO audioDAO,
    WorkManager workManager,
    InstrumentMemeDAO instrumentMemeDAO
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.instrumentDAO = instrumentDAO;
    this.audioDAO = audioDAO;
    this.workManager = workManager;
    this.instrumentMemeDAO = instrumentMemeDAO;
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
   Do Instrument Clone ExpectationOfWork
   Worker removes all child entities for the Instrument
   Worker deletes all S3 objects for the Instrument
   Worker deletes the Instrument
   */
  private void doWork() throws Exception {
    Instrument from = instrumentDAO.readOne(Access.internal(), fromId);
    if (Objects.isNull(from))
      throw new BusinessException("Could not fetch clone source Instrument");

    Instrument to = instrumentDAO.readOne(Access.internal(), toId);
    if (Objects.isNull(to))
      throw new BusinessException("Could not fetch clone target Instrument");

    // Clone InstrumentMeme
    instrumentMemeDAO.readAll(Access.internal(), fromId).forEach(instrumentMeme -> {
      instrumentMeme.setInstrumentId(toId);

      try {
        InstrumentMeme toInstrumentMeme = instrumentMemeDAO.create(Access.internal(), instrumentMeme);
        log.info("Cloned InstrumentMeme from #{} to {}", instrumentMeme.getId(), JSON.objectFrom(toInstrumentMeme));

      } catch (Exception e) {
        log.error("Failed to clone InstrumentMeme {}", JSON.objectFrom(instrumentMeme), e);
      }
    });

    // Clone each Audio and schedule an AudioClone job
    audioDAO.readAll(Access.internal(), fromId).forEach(audio -> {
      audio.setInstrumentId(toId);
      BigInteger fromAudioId = audio.getId();

      try {
        Audio toAudio = audioDAO.create(Access.internal(), audio);
        workManager.scheduleAudioClone(0, fromAudioId, toAudio.getId());
        log.info("Cloned Audio from #{} to {} and scheduled AudioClone job", audio.getId(), JSON.objectFrom(toAudio));

      } catch (Exception e) {
        log.error("Failed to clone Audio {}", JSON.objectFrom(audio), e);
      }
    });

    log.info("Cloned Instrument #{} and child entities to new Instrument {}", fromId, JSON.objectFrom(to));
  }


}
