// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.craft.isometry.VoiceIsometry;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.transport.JSON;
import io.xj.worker.job.PatternCloneJob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class PatternCloneJobImpl implements PatternCloneJob {
  static final Logger log = LoggerFactory.getLogger(PatternCloneJobImpl.class);
  private final BigInteger toId;
  private final PatternDAO patternDAO;
  private final BigInteger fromId;
  private final PatternMemeDAO patternMemeDAO;
  private final PatternChordDAO patternChordDAO;
  private final PatternEventDAO patternEventDAO;
  private final VoiceDAO voiceDAO;

  @Inject
  public PatternCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    PatternDAO patternDAO,
    PatternMemeDAO patternMemeDAO,
    PatternChordDAO patternChordDAO,
    PatternEventDAO patternEventDAO,
    VoiceDAO voiceDAO
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.patternDAO = patternDAO;
    this.patternMemeDAO = patternMemeDAO;
    this.patternChordDAO = patternChordDAO;
    this.patternEventDAO = patternEventDAO;
    this.voiceDAO = voiceDAO;
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
    patternMemeDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(patternMeme -> {
      patternMeme.setPatternId(toId);

      try {
        PatternMeme toPatternMeme = patternMemeDAO.create(Access.internal(), patternMeme);
        log.info("Cloned PatternMeme ofMemes #{} to {}", patternMeme.getId(), JSON.objectFrom(toPatternMeme));

      } catch (Exception e) {
        log.error("Failed to clone PatternMeme {}", JSON.objectFrom(patternMeme), e);
      }
    });

    // Clone PatternChord
    patternChordDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(patternChord -> {
      patternChord.setPatternId(toId);

      try {
        PatternChord toPatternChord = patternChordDAO.create(Access.internal(), patternChord);
        log.info("Cloned PatternChord ofMemes #{} to {}", patternChord.getId(), JSON.objectFrom(toPatternChord));

      } catch (Exception e) {
        log.error("Failed to clone PatternChord {}", JSON.objectFrom(patternChord), e);
      }
    });

    // In order to assign cloned voice events to their new voice
    // Get all voices from source and (optionally, if different) target sequences;
    // map source to target voice ids
    Map<BigInteger, BigInteger> voiceCloneIds = Maps.newConcurrentMap();
    Collection<Voice> sourceVoices = voiceDAO.readAll(Access.internal(), ImmutableList.of(from.getSequenceId()));
    if (Objects.equals(from.getSequenceId(), to.getSequenceId()))
      sourceVoices.forEach((voice) -> voiceCloneIds.put(voice.getId(), voice.getId()));
    else {
      VoiceIsometry targetVoices = VoiceIsometry.ofVoices(voiceDAO.readAll(Access.internal(), ImmutableList.of(to.getSequenceId())));
      sourceVoices.forEach((sourceVoice) -> {
        Voice targetVoice = targetVoices.find(sourceVoice);
        if (Objects.nonNull(targetVoice))
          voiceCloneIds.put(sourceVoice.getId(), targetVoice.getId());
      });
    }

    //  Clone each PatternEvent
    patternEventDAO.readAll(Access.internal(), ImmutableList.of(fromId)).forEach(fromPatternEvent -> {
      fromPatternEvent.setPatternId(toId);
      BigInteger toVoiceId = voiceCloneIds.getOrDefault(fromPatternEvent.getVoiceId(), null);
      if (Objects.isNull(toVoiceId)) return;
      fromPatternEvent.setVoiceId(toVoiceId);

      try {
        PatternEvent toPatternEvent = patternEventDAO.create(Access.internal(), fromPatternEvent);
        log.info("Cloned PatternEvent ofMemes #{} to {}", fromPatternEvent.getId(), JSON.objectFrom(toPatternEvent));

      } catch (Exception e) {
        log.error("Failed to clone PatternEvent ofMemes {}", JSON.objectFrom(fromPatternEvent), e);
      }
    });

    log.info("Cloned Pattern #{} and child entities to new Pattern {}", fromId, JSON.objectFrom(to));
  }


}
