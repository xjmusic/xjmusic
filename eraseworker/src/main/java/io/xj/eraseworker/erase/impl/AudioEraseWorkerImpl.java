// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.eraseworker.erase.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.model.audio.AudioState;
import io.xj.core.tables.records.AudioRecord;
import io.xj.eraseworker.erase.AudioEraseWorker;

import org.jooq.Result;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioEraseWorkerImpl implements AudioEraseWorker {
  private final static Logger log = LoggerFactory.getLogger(AudioEraseWorkerImpl.class);
  private final Integer batchSize;
  private final AudioDAO audioDAO;

  @Inject
  public AudioEraseWorkerImpl(
    @Assisted("batchSize") Integer batchSize,
    AudioDAO audioDAO
  ) {
    this.batchSize = batchSize;
    this.audioDAO = audioDAO;
  }

  @Override
  public Runnable getTaskRunnable() throws Exception {
    return new AudioEraseWorkerTaskRunner();
  }

  /**
   This runnable is executed in a thread pool
   */
  public class AudioEraseWorkerTaskRunner implements Runnable {

    @Override
    public void run() {
      try {
        for (AudioRecord audio : getAudiosToErase())
          erase(audio);

      } catch (Exception e) {
        log.error("{}:{} failed with exception {}",
          this.getClass().getSimpleName(), Thread.currentThread().getName(), e);
      }
    }

    /**
     Do Audio Erase Work
     Eraseworker removes all child entities for the Audio
     Eraseworker deletes all S3 objects for the Audio
     Eraseworker deletes the Audio
     */
    private void erase(AudioRecord audio) throws Exception {
      audioDAO.destroy(Access.internal(), audio.getId());
      log.info("Erased Audio #{}, destroyed child entities, and deleted s3 object {}", audio.getId(), audio.getWaveformKey());
    }

    /**
     Get audios in Erase state
     Eraseworker finds any Audio in ERASE state to work on

     @return audios
     @throws Exception on failure
     */
    private Result<AudioRecord> getAudiosToErase() throws Exception {
      return audioDAO.readAllInState(Access.internal(), AudioState.Erase, batchSize);
    }

  }

}
