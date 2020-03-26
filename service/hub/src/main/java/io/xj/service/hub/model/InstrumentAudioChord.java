// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.ChordEntity;

import java.util.UUID;

/**
 + [#166708597] Audio model handles all of its own entities
 */
public class InstrumentAudioChord extends ChordEntity {


  private UUID instrumentId;
  private UUID instrumentAudioId;
  private String name;
  private Double position;

  /**
   Get AudioChord of a specified name

   @param name to get AudioChord of
   @return AudioChord
   */
  public static InstrumentAudioChord create
  (String name) {
    return new InstrumentAudioChord().setName(name);
  }

  /**
   Create a new AudioChord

   @param audio    to of chord in
   @param position of chord
   @param name     of chord
   @return new audio chord
   */
  public static InstrumentAudioChord create(InstrumentAudio audio, double position, String name) {
    return create()
      .setInstrumentId(audio.getInstrumentId())
      .setInstrumentAudioId(audio.getId())
      .setPosition(position)
      .setName(name);
  }

  /**
   Create a new AudioChord

   @return new audio chord
   */
  public static InstrumentAudioChord create() {
    return (InstrumentAudioChord) new InstrumentAudioChord().setId(UUID.randomUUID());
  }

  /**
   Get id of Instrument to which this entity belongs

   @return instrument id
   */
  public UUID getInstrumentId() {
    return instrumentId;
  }

  /**
   Set id of Instrument to which this entity belongs

   @param instrumentId to which this entity belongs
   @return this Instrument Entity (for chaining setters)
   */
  public InstrumentAudioChord setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public UUID getParentId() {
    return instrumentId;
  }

  /**
   get Audio UUID

   @return Audio UUID
   */
  public UUID getInstrumentAudioId() {
    return instrumentAudioId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  /**
   Set Audio UUID by providing the parent Audio

   @param audio of which to set audio id
   @return this AudioChord (for chaining methods)
   */
  public InstrumentAudioChord setAudio(InstrumentAudio audio) {
    return setInstrumentAudioId(audio.getId());
  }

  /**
   Set Audio UUID

   @param instrumentAudioId to set
   @return this AudioChord (for chaining methods)
   */
  public InstrumentAudioChord setInstrumentAudioId(UUID instrumentAudioId) {
    this.instrumentAudioId = instrumentAudioId;
    return this;
  }

  @Override
  public InstrumentAudioChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public InstrumentAudioChord setPosition(Double position) {
    this.position = Value.limitDecimalPrecision(position);
    return this;
  }

  @Override
  public io.xj.lib.music.Chord toMusical() {
    return new io.xj.lib.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

  @Override
  public void validate() throws ValueException {
    Value.require(instrumentId, "Instrument ID");
    Value.require(instrumentAudioId, "Instrument Audio ID");

    ChordEntity.validate(this);
  }
}
