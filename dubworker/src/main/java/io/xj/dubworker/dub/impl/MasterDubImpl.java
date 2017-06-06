// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.dubworker.dub.impl;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.basis.Basis;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.pick.Pick;
import io.xj.dubworker.dub.MasterDub;
import io.xj.mixer.Mixer;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.OutputContainer;

import org.jooq.types.ULong;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.BufferedInputStream;
import java.util.Objects;

/**
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public class MasterDubImpl implements MasterDub {
  //  private final Logger log = LoggerFactory.getLogger(MasterDubImpl.class);
  private final Basis basis;
  private final MixerFactory mixerFactory;
  private Mixer _mixer;

  @Inject
  public MasterDubImpl(
    @Assisted("basis") Basis basis,
    MixerFactory mixerFactory
  /*-*/) throws BusinessException {
    this.basis = basis;
    this.mixerFactory = mixerFactory;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      doMixerSourceLoading();
      doMixerTargetSetting();
      doMix();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type MasterDub for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }

  /**
   Implements Mixer module to load all waveform for Audio in current Link
   */
  private void doMixerSourceLoading() throws Exception {
    for (ULong audioId : basis.linkAudioIds())
      setupSourceFromStream(audioId.toString(),
        basis.streamAudioWaveform(basis.linkAudio(audioId)));
  }

  /**
   Setup one input source from a stream, then close the stream

   @param sourceId    to setup source as
   @param inputStream to setup source from
   @throws Exception on failure
   */
  private void setupSourceFromStream(String sourceId, BufferedInputStream inputStream) throws Exception {
    mixer().loadSource(sourceId, inputStream);
    inputStream.close();
  }

  /**
   Implements Mixer module to set playback for Picks in current Link
   */
  private void doMixerTargetSetting() throws Exception {
    for (Pick pick : basis.picks())
      setupTarget(pick);
  }

  /**
   Set playback for a pick
   <p>
   [#283] Pitch ratio should result in lower audio playback for lower note
   [#341] Dubworker takes into account the start offset of each audio, in order to ensure that it is mixed such that the hit is exactly on the meter

   @param pick to set playback for
   */
  private void setupTarget(Pick pick) throws Exception {
    double pitchRatio = basis.linkAudio(pick.getAudioId()).getPitch() / pick.getPitch();
    double offsetStart = basis.linkAudio(pick.getAudioId()).getStart() / pitchRatio;

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
    // mix it
    mixer().mixToFile(basis.outputFilePath());
  }

  /**
   Get a mixer instance
   (caches instance)

   @return mixer
   */
  private Mixer mixer() throws Exception {
    if (Objects.isNull(_mixer))
      _mixer = mixerFactory.createMixer(
        outputAudioContainer(),
        basis.outputAudioFormat(),
        basis.linkTotalLength());

    return _mixer;
  }

  /**
   get output audio container from chain config

   @return output container
   @throws Exception on failure
   */
  private OutputContainer outputAudioContainer() throws Exception {
    return OutputContainer.valueOf(basis.chainConfig(ChainConfigType.OutputContainer).getValue());
  }

  /**
   Report
   */
  private void report() {
    // basis.report() anything else interesting from the dub operation
  }

}
