// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master.impl;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.access.impl.Access;
import io.xj.core.cache.audio.AudioCacheProvider;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.dub.master.MasterDub;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pick.Pick;
import io.xj.core.util.Text;
import io.xj.craft.basis.Basis;
import io.xj.mixer.Mixer;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.OutputEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MasterDubImpl implements MasterDub {
  private final Logger log = LoggerFactory.getLogger(MasterDubImpl.class);
  private final Basis basis;
  private final MixerFactory mixerFactory;
  private final SegmentMessageDAO segmentMessageDAO;
  private Mixer _mixer;
  private List<String> warnings = Lists.newArrayList();
  private AudioCacheProvider audioCacheProvider;

  @Inject
  public MasterDubImpl(
    @Assisted("basis") Basis basis,
    AudioCacheProvider audioCacheProvider,
    SegmentMessageDAO segmentMessageDAO,
    MixerFactory mixerFactory
    /*-*/) throws BusinessException {
    this.audioCacheProvider = audioCacheProvider;
    this.basis = basis;
    this.segmentMessageDAO = segmentMessageDAO;
    this.mixerFactory = mixerFactory;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      doMixerSourceLoading();
      doMixerTargetSetting();
      doMix();
      reportWarnings();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type MasterDub for segment #%s",
          basis.type(), basis.segment().getId().toString()), e);
    }
  }

  /**
   @throws Exception if failed to stream data of item from cache
   */
  private void doMixerSourceLoading() throws Exception {
    for (BigInteger audioId : basis.segmentAudioIds()) {
      Audio audio = basis.segmentAudio(audioId);
      String key = audio.getWaveformKey();

      try {
        InputStream stream = audioCacheProvider.get(key).stream();
        mixer().loadSource(audio.getId().toString(), new BufferedInputStream(stream));
        stream.close();

      } catch (Exception e) {
        audioCacheProvider.refresh(key);
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
    }
  }

  /**
   Implements Mixer module to set playback for Picks in current Segment
   */
  private void doMixerTargetSetting() throws Exception {
    for (Pick pick : basis.picks())
      try {
        setupTarget(pick);
      } catch (Exception e) {
        warnings.add(e.getMessage() + " " + Text.formatStackTrace(e));
      }
  }

  /**
   Set playback for a pick
   <p>
   [#283] Pitch ratio should result in lower audio playback for lower note
   [#341] Dubworker takes into account the start offset of each audio, in order to ensure that it is mixed such that the hit is exactly on the meter

   @param pick to set playback for
   */
  private void setupTarget(Pick pick) throws Exception {
    double pitchRatio = basis.segmentAudio(pick.getAudioId()).getPitch() / pick.getPitch();
    double offsetStart = basis.segmentAudio(pick.getAudioId()).getStart() / pitchRatio;

    mixer().put(
      pick.getAudioId().toString(),
      basis.atMicros(pick.getStart() - offsetStart),
      basis.atMicros(pick.getStart() + pick.getLength()),
      pick.getAmplitude(),
      pitchRatio,
      0);
  }

  /**
   MasterDub implements Mixer module to mix final output to waveform streamed directly to Amazon S3
   */
  private void doMix() throws Exception {
    float quality = Float.valueOf(basis.chainConfig(ChainConfigType.OutputEncodingQuality).getValue());
    mixer().mixToFile(OutputEncoder.OGG_VORBIS, basis.outputFilePath(), quality);
  }

  /**
   Get a mixer instance
   (caches instance)

   @return mixer
   */
  private Mixer mixer() throws Exception {
    if (Objects.isNull(_mixer))
      _mixer = mixerFactory.createMixer(
        basis.outputAudioFormat(),
        basis.segmentTotalLength());

    return _mixer;
  }

  /**
   get output audio container from chain config

   @return output container
   @throws Exception on failure
   */
  private OutputEncoder outputAudioContainer() throws Exception {
    return OutputEncoder.valueOf(basis.chainConfig(ChainConfigType.OutputContainer).getValue());
  }

  /**
   Report
   */
  private void report() {
    // basis.report() anything else interesting from the dub operation
  }

  /**
   Report warnings in a concatenated Segment Message
   */
  private void reportWarnings() {
    if (warnings.isEmpty()) return;

    StringBuilder body = new StringBuilder("MasterDub had warnings:");
    for (String warning : warnings) {
      body.append(String.format("%n%n%s", warning));
    }
    createSegmentMessage(MessageType.Warning, body.toString());
  }

  /**
   [#226] Messages pertaining to a Segment

   @param type of message
   @param body of message
   */
  private void createSegmentMessage(MessageType type, String body) {
    try {
      segmentMessageDAO.create(Access.internal(), new SegmentMessage()
        .setType(type.toString())
        .setSegmentId(basis.segment().getId())
        .setBody(body));
    } catch (Exception e1) {
      log.warn("Failed to create SegmentMessage", e1);
    }
  }

}
