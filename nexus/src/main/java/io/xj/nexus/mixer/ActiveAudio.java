// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.mixer;

import io.xj.hub.InstrumentConfig;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
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
  private final float amplitude;

  public ActiveAudio(
    SegmentChoiceArrangementPick pick,
    Instrument instrument,
    InstrumentAudio audio,
    Long startAtMixerMicros,
    @Nullable Long stopAtMixerMicros,
    float intensityAmplitude
  ) {
    this.pick = pick;
    this.audio = audio;
    this.startAtMixerMicros = startAtMixerMicros;
    this.stopAtMixerMicros = stopAtMixerMicros;
    this.instrument = instrument;

    // computed
    this.amplitude = intensityAmplitude * pick.getAmplitude() * instrument.getVolume() * audio.getVolume();
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

  public float getAmplitude() {
    return amplitude;
  }
}
