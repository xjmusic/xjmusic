// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.model.audio.Audio;
import io.xj.craft.exception.CraftException;
import io.xj.worker.job.AudioEraseJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class AudioEraseJobImpl implements AudioEraseJob {
  static final Logger log = LoggerFactory.getLogger(AudioEraseJobImpl.class);
  private final AudioDAO audioDAO;
  private final BigInteger entityId;
  private final Access access = Access.internal();

  @Inject
  public AudioEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    AudioDAO audioDAO
  ) {
    this.entityId = entityId;
    this.audioDAO = audioDAO;
  }

  @Override
  public void run() {
    try {
      Audio audio = audioDAO.readOne(access, entityId);
      doWork(audio);

    } catch (CraftException e) {
      log.warn("Did not erase audioId={}, reason={}", entityId, e.getMessage());

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Audio Erase Work
   Eraseworker removes all child entities for the Audio
   Eraseworker deletes all S3 objects for the Audio
   Eraseworker deletes the Audio
   */
  private void doWork(Audio audio) throws Exception {
    audioDAO.destroy(access, audio.getId());
    log.info("Erased Audio #{}, destroyed child entities, and deleted s3 object {}", audio.getId(), audio.getWaveformKey());
  }

}
