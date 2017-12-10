// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.tables.records.AudioRecord;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.AudioEraseJob;

import org.jooq.types.ULong;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AudioEraseJobImpl implements AudioEraseJob {
  static final Logger log = LoggerFactory.getLogger(AudioEraseJobImpl.class);
  private final AudioDAO audioDAO;
  private final ULong entityId;
  private WorkManager workManager;

  @Inject
  public AudioEraseJobImpl(
    @Assisted("entityId") ULong entityId,
    AudioDAO audioDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.audioDAO = audioDAO;
    this.workManager = workManager;
  }


  @Override
  public void run() {
    try {
      AudioRecord audio = audioDAO.readOne(Access.internal(), entityId);
      if (Objects.nonNull(audio)) {
        erase(audio);
      }
      workManager.stopAudioErase(entityId);

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Audio Erase ExpectationOfWork
   Eraseworker removes all child entities for the Audio
   Eraseworker deletes all S3 objects for the Audio
   Eraseworker deletes the Audio
   */
  private void erase(AudioRecord audio) throws Exception {
    audioDAO.destroy(Access.internal(), audio.getId());
    log.info("Erased Audio #{}, destroyed child entities, and deleted s3 object {}", audio.getId(), audio.getWaveformKey());
  }

}
