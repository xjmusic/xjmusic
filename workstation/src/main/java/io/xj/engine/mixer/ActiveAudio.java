// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.mixer;

import io.xj.model.InstrumentConfig;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.engine.model.SegmentChoiceArrangementPick;
import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ActiveAudio {
  private final InstrumentConfig instrumentConfig;
  private final SegmentChoiceArrangementPick pick;
  private final InstrumentAudio audio;
  private final Long startAtMixerMicros;
  @Nullable
  private final Long stopAtMixerMicros;
  private final Instrument instrument;
  private final float fromAmplitude;
  private final float toAmplitude;

  public ActiveAudio(
      SegmentChoiceArrangementPick pick,
      Instrument instrument,
      InstrumentAudio audio,
      Long startAtMixerMicros,
      @Nullable Long stopAtMixerMicros,
      float fromIntensityAmplitude,
      float toIntensityAmplitude
  ) {
    this.pick = pick;
    this.audio = audio;
    this.startAtMixerMicros = startAtMixerMicros;
    this.stopAtMixerMicros = stopAtMixerMicros;
    this.instrument = instrument;

    // computed
    this.fromAmplitude = fromIntensityAmplitude * pick.getAmplitude() * instrument.getVolume() * audio.getVolume();
    this.toAmplitude = toIntensityAmplitude * pick.getAmplitude() * instrument.getVolume() * audio.getVolume();
    this.instrumentConfig = new InstrumentConfig(instrument);
  }

  public UUID getId() {
    return pick.getId();
  }

  public SegmentChoiceArrangementPick getPick() {
    return pick;
  }

  public Instrument getInstrument() {
    return instrument;
  }

  public Long getStartAtMixerMicros() {
    return startAtMixerMicros;
  }

  public Optional<Long> getStopAtMixerMicros() {
    return Optional.ofNullable(stopAtMixerMicros);
  }

  public InstrumentAudio getAudio() {
    return audio;
  }

  public int getReleaseMillis() {
    return instrumentConfig.getReleaseMillis();
  }

  /**
   Get the amplitude at a given amplitude position between 0 and 1.

   @param ap amplitude position
   @return amplitude
   */
  public float getAmplitude(float ap) {
    if (fromAmplitude == toAmplitude) return fromAmplitude;
    return fromAmplitude + (toAmplitude - fromAmplitude) * ap;
  }
}
