// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.transport.JSON;
import io.xj.worker.job.AudioCloneJob;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class AudioCloneJobImpl implements AudioCloneJob {
  static final Logger log = LoggerFactory.getLogger(AudioCloneJobImpl.class);
  private final BigInteger toId;
  private final AudioDAO audioDAO;
  private final BigInteger fromId;
  private final AudioEventDAO audioEventDAO;
  private final AudioChordDAO audioChordDAO;
  private final AmazonProvider amazonProvider;

  @Inject
  public AudioCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
    AudioChordDAO audioChordDAO,
    AmazonProvider amazonProvider
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.audioDAO = audioDAO;
    this.audioEventDAO = audioEventDAO;
    this.audioChordDAO = audioChordDAO;
    this.amazonProvider = amazonProvider;
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
   Do Audio Clone ExpectationOfWork
   Worker removes all child entities for the Audio
   Worker deletes all S3 objects for the Audio
   Worker deletes the Audio
   */
  private void doWork() throws Exception {
    Audio from = audioDAO.readOne(Access.internal(), fromId);
    if (Objects.isNull(from))
      throw new BusinessException("Could not fetch clone source Audio");

    Audio to = audioDAO.readOne(Access.internal(), toId);
    if (Objects.isNull(to))
      throw new BusinessException("Could not fetch clone target Audio");

    // Clone AudioEvent
    audioEventDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(audioEvent -> {
      audioEvent.setAudioId(toId);

      try {
        AudioEvent toAudioEvent = audioEventDAO.create(Access.internal(), audioEvent);
        log.info("Cloned AudioEvent from #{} to {}", audioEvent.getId(), JSON.objectFrom(toAudioEvent));


      } catch (Exception e) {
        log.error("Failed to clone AudioEvent {}", JSON.objectFrom(audioEvent), e);
      }
    });

    // Clone AudioChord
    audioChordDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(audioChord -> {
      audioChord.setAudioId(toId);

      try {
        AudioChord toAudioChord = audioChordDAO.create(Access.internal(), audioChord);
        log.info("Cloned AudioChord from #{} to {}", audioChord.getId(), JSON.objectFrom(toAudioChord));

      } catch (Exception e) {
        log.error("Failed to clone AudioChord {}", JSON.objectFrom(audioChord), e);
      }
    });

    // Clone S3 Data
    amazonProvider.copyS3Object(Config.audioFileBucket(), from.getWaveformKey(), Config.audioFileBucket(), to.getWaveformKey());
    log.info("Copied S3 Object from {}:{} to {}:{}", Config.audioFileBucket(), from.getWaveformKey(), Config.audioFileBucket(), to.getWaveformKey());

    log.info("Cloned Audio #{} and child entities to new Audio {}", fromId, JSON.objectFrom(to));
  }

}
