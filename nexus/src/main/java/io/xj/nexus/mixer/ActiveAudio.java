// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.mixer;

import io.xj.hub.InstrumentConfig;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import org.jetbrains.annotations.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ActiveAudio {
  final InstrumentConfig instrumentConfig;
  SegmentChoiceArrangementPick pick;
  final InstrumentAudio audio;
  final Long startAtMicros;
  @Nullable
  final Long stopAtMicros;
  MixerPickState state;
  final Instrument instrument;

  public ActiveAudio(SegmentChoiceArrangementPick pick, Instrument instrument, InstrumentAudio audio, Long startAtMicros, @Nullable Long stopAtMicros) {
    this.pick = pick;
    this.audio = audio;
    this.startAtMicros = startAtMicros;
    this.stopAtMicros = stopAtMicros;
    this.instrument = instrument;
    this.instrumentConfig = new InstrumentConfig(instrument);
    state = MixerPickState.PLANNED;
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

  public InstrumentConfig getInstrumentConfig() {
    return instrumentConfig;
  }

  public Long getStartAtMicros() {
    return startAtMicros;
  }

  public Optional<Long> getStopAtMicros() {
    return Optional.ofNullable(stopAtMicros);
  }

  public InstrumentAudio getAudio() {
    return audio;
  }

  public MixerPickState getState() {
    return state;
  }

  public double getAudioVolume() {
    return audio.getVolume() * instrument.getVolume();
  }

  public int getAttackMillis() {
    return instrumentConfig.getAttackMillis();
  }

  public int getReleaseMillis() {
    return instrumentConfig.getReleaseMillis();
  }
}
