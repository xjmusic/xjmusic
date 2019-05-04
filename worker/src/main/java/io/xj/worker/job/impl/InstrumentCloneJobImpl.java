// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.InstrumentCloneJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class InstrumentCloneJobImpl implements InstrumentCloneJob {
  static final Logger log = LoggerFactory.getLogger(InstrumentCloneJobImpl.class);
  private final BigInteger toId;
  private final InstrumentDAO instrumentDAO;
  private final BigInteger fromId;
  private final AudioDAO audioDAO;
  private final WorkManager workManager;
  private final InstrumentMemeDAO instrumentMemeDAO;
  private final Access access = Access.internal();

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
      doWork(instrumentDAO.readOne(access, fromId), instrumentDAO.readOne(access, toId));

    } catch (CoreException e) {
      log.warn("Did not clone Instrument fromId={}, toId={}, reason={}", fromId, toId, e.getMessage());

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Instrument Clone Work
   Worker removes all child entities for the Instrument
   Worker deletes all S3 objects for the Instrument
   Worker deletes the Instrument@param instrument

   @param from instrument to clone
   @param to   target instrument to be cloned onto (already created)
   @throws CoreException on failure
   */
  private void doWork(Instrument from, Instrument to) throws CoreException {
    cloneAllInstrumentMemes(from, to);
    cloneAllAudioSAndScheduleAudioCloneJob(from, to);
    log.info("Cloned Instrument #{} and child entities to new Instrument {}", from.getId(), to);
  }

  /**
   Clone all Audio from one Instrument to another, and schedule an AudioClone job for each.

   @param from Instrument
   @param to   Instrument
   */
  private void cloneAllAudioSAndScheduleAudioCloneJob(Instrument from, Instrument to) throws CoreException {
    audioDAO.readAll(access, ImmutableList.of(from.getId())).forEach(audio -> {
      audio.setInstrumentId(to.getId());
      BigInteger fromAudioId = audio.getId();

      try {
        Audio toAudio = audioDAO.create(access, audio);
        workManager.doAudioClone(fromAudioId, toAudio.getId());
        log.info("Cloned Audio of Instrument #{} to {} and scheduled AudioClone job", audio.getId(), toAudio);

      } catch (Exception e) {
        log.error("Failed to clone Audio {}", audio, e);
      }
    });
  }

  /**
   Clone all InstrumentMeme from one Instrument to another

   @param from Instrument
   @param to   Instrument
   */
  private void cloneAllInstrumentMemes(Instrument from, Instrument to) throws CoreException {
    instrumentMemeDAO.readAll(access, ImmutableList.of(from.getId())).forEach(instrumentMeme -> {
      instrumentMeme.setInstrumentId(to.getId());

      try {
        InstrumentMeme toInstrumentMeme = instrumentMemeDAO.create(access, instrumentMeme);
        log.info("Cloned InstrumentMeme of Instrument #{} to {}", instrumentMeme.getId(), toInstrumentMeme);

      } catch (Exception e) {
        log.error("Failed to clone InstrumentMeme {}", instrumentMeme, e);
      }
    });
  }


}
