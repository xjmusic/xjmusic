// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.isometry.VoiceIsometry;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.voice.Voice;
import io.xj.worker.job.PatternCloneJob;
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
  private final PatternChordDAO patternChordDAO;
  private final PatternEventDAO patternEventDAO;
  private final VoiceDAO voiceDAO;
  private final Access access = Access.internal();

  @Inject
  public PatternCloneJobImpl(
    @Assisted("fromId") BigInteger fromId,
    @Assisted("toId") BigInteger toId,
    PatternDAO patternDAO,
    PatternChordDAO patternChordDAO,
    PatternEventDAO patternEventDAO,
    VoiceDAO voiceDAO
  ) {
    this.fromId = fromId;
    this.toId = toId;
    this.patternDAO = patternDAO;
    this.patternChordDAO = patternChordDAO;
    this.patternEventDAO = patternEventDAO;
    this.voiceDAO = voiceDAO;
  }

  @Override
  public void run() {
    try {
      doWork(patternDAO.readOne(access, fromId), patternDAO.readOne(access, toId));

    } catch (CoreException e) {
      log.warn("Did not clone Pattern fromId={}, toId={}, reason={}", fromId, toId, e.getMessage());

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Pattern Clone Work
   Worker removes all child entities for the Pattern
   Worker deletes all S3 objects for the Pattern
   Worker deletes the Pattern

   @param from to clone
   @param to   to clone onto (already created)
   */
  private void doWork(Pattern from, Pattern to) throws Exception {
    cloneAllPatternChords(from, to);
    cloneAllPatternEvents(buildVoiceCloneIdMap(from, to), from, to);
    log.info("Cloned Pattern #{} and child entities to new Pattern {}", fromId, to);
  }

  /**
   Clone all PatternEvent from one Pattern to another

   @param voiceCloneIdMap of the id of voices in source pattern, to id of corresponding voice in target pattern
   @param from            Pattern
   @param to              Pattern
   */
  private void cloneAllPatternEvents(Map<BigInteger, BigInteger> voiceCloneIdMap, Pattern from, Pattern to) throws CoreException {
    patternEventDAO.readAll(access, ImmutableList.of(from.getId())).forEach(fromPatternEvent -> {
      fromPatternEvent.setPatternId(to.getId());
      BigInteger toVoiceId = voiceCloneIdMap.getOrDefault(fromPatternEvent.getVoiceId(), null);
      if (Objects.isNull(toVoiceId)) return;
      fromPatternEvent.setVoiceId(toVoiceId);

      try {
        PatternEvent toPatternEvent = patternEventDAO.create(access, fromPatternEvent);
        log.info("Cloned PatternEvent ofMemes #{} to {}", fromPatternEvent.getId(), toPatternEvent);

      } catch (Exception e) {
        log.error("Failed to clone PatternEvent ofMemes {}", fromPatternEvent, e);
      }
    });
  }

  /**
   Clone all PatternChords from one pattern to another

   @param from Pattern
   @param to   Pattern
   */
  private void cloneAllPatternChords(Pattern from, Pattern to) throws CoreException {
    patternChordDAO.readAll(access, ImmutableList.of(from.getId())).forEach(patternChord -> {
      patternChord.setPatternId(to.getId());

      try {
        PatternChord toPatternChord = patternChordDAO.create(access, patternChord);
        log.info("Cloned PatternChord ofMemes #{} to {}", patternChord.getId(), toPatternChord);

      } catch (Exception e) {
        log.error("Failed to clone PatternChord {}", patternChord, e);
      }
    });
  }


  /**
   In order to assign cloned voice events to their new voice
   Get all voices from source and (optionally, if different) target sequences;
   map source to target voice ids

   @param from Pattern
   @param to   Pattern
   @return map of the id of voices in source pattern, to id of corresponding voice in target pattern
   @throws CoreException on failure
   */
  private Map<BigInteger, BigInteger> buildVoiceCloneIdMap(Pattern from, Pattern to) throws CoreException {
    Map<BigInteger, BigInteger> voiceCloneIdMap = Maps.newConcurrentMap();
    Collection<Voice> sourceVoices = voiceDAO.readAll(access, ImmutableList.of(from.getSequenceId()));
    if (Objects.equals(from.getSequenceId(), to.getSequenceId()))
      sourceVoices.forEach((voice) -> voiceCloneIdMap.put(voice.getId(), voice.getId()));
    else {
      VoiceIsometry targetVoices = VoiceIsometry.ofVoices(voiceDAO.readAll(access, ImmutableList.of(to.getSequenceId())));
      sourceVoices.forEach((sourceVoice) -> {
        Voice targetVoice = targetVoices.find(sourceVoice);
        if (Objects.nonNull(targetVoice))
          voiceCloneIdMap.put(sourceVoice.getId(), targetVoice.getId());
      });
    }
    return voiceCloneIdMap;
  }

}
