// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.worker.job.AudioCloneJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class AudioCloneJobImpl implements AudioCloneJob {
  static final Logger log = LoggerFactory.getLogger(AudioCloneJobImpl.class);
  private final BigInteger toId;
  private final AudioDAO audioDAO;
  private final BigInteger fromId;
  private final AudioEventDAO audioEventDAO;
  private final AudioChordDAO audioChordDAO;
  private final AmazonProvider amazonProvider;
  private final Access access = Access.internal();

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
      doWork(audioDAO.readOne(access, fromId), audioDAO.readOne(access, toId));

    } catch (CoreException e) {
      log.warn("Did not clone Audio fromId={}, toId={}, reason={}", fromId, toId, e.getMessage());

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Audio Clone Work
   Worker removes all child entities for the Audio
   Worker deletes all S3 objects for the Audio
   Worker deletes the Audio

   @param from Audio to clone
   @param to   Target Audio to be cloned to (already created)
   @throws CoreException on Failure
   */
  private void doWork(Audio from, Audio to) throws CoreException {
    cloneAllAudioEvents(from, to);
    cloneAllAudioChords(from, to);
    cloneS3Data(from, to);
    log.info("Cloned Audio #{} and child entities to new Audio {}", from.getId(), to);
  }

  /**
   Clone all AudioEvent from one Audio to another

   @param from Audio
   @param to   Audio
   */
  private void cloneAllAudioEvents(Audio from, Audio to) throws CoreException {
    audioEventDAO.readAll(access, ImmutableList.of(from.getId())).forEach(audioEvent -> {
      audioEvent.setAudioId(to.getId());

      try {
        AudioEvent toAudioEvent = audioEventDAO.create(access, audioEvent);
        log.info("Cloned AudioEvent ofMemes #{} to {}", audioEvent.getId(), toAudioEvent);

      } catch (Exception e) {
        log.error("Failed to clone AudioEvent {}", audioEvent, e);
      }
    });
  }

  /**
   Clone all AudioChord from one Audio to another

   @param from Audio
   @param to   Audio
   */
  private void cloneAllAudioChords(Audio from, Audio to) throws CoreException {
    audioChordDAO.readAll(access, ImmutableList.of(from.getId())).forEach(audioChord ->
    {
      audioChord.setAudioId(to.getId());

      try {
        AudioChord toAudioChord = audioChordDAO.create(access, audioChord);
        log.info("Cloned AudioChord ofMemes #{} to {}", audioChord.getId(), toAudioChord);

      } catch (Exception e) {
        log.error("Failed to clone AudioChord {}", audioChord, e);
      }
    });
  }

  /**
   Clone S3 Data from one Audio to another

   @param from Audio
   @param to   Audio
   */
  private void cloneS3Data(Audio from, Audio to) throws CoreException {
    amazonProvider.copyS3Object(Config.audioFileBucket(), from.getWaveformKey(), Config.audioFileBucket(), to.getWaveformKey());
    log.info("Copied S3 Object ofMemes {}:{} to {}:{}", Config.audioFileBucket(), from.getWaveformKey(), Config.audioFileBucket(), to.getWaveformKey());
  }
}
