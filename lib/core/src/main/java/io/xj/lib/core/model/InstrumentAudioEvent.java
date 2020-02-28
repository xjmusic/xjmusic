// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.EventEntity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.util.Text;

import java.util.UUID;

/**
 + [#166708597] Audio model handles all of its own entities
 */
public class InstrumentAudioEvent extends EventEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(EventEntity.RESOURCE_ATTRIBUTE_NAMES)
    .add("name")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Instrument.class)
    .add(InstrumentAudio.class)
    .build();
  private UUID instrumentId;
  private UUID instrumentAudioId;
  private String name;

  /**
   Create a new AudioEvent

   @param instrumentAudio    to of event in
   @param position of AudioEvent
   @param duration of AudioEvent
   @param name     of AudioEvent
   @param note     of AudioEvent
   @param velocity of AudioEvent
   @return new AudioEvent
   */
  public static InstrumentAudioEvent create(InstrumentAudio instrumentAudio, double position, double duration, String name, String note, double velocity) {
    return create(instrumentAudio, position, name)
      .setInstrumentId(instrumentAudio.getInstrumentId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setDuration(duration)
      .setNote(note)
      .setVelocity(velocity);
  }

  /**
   Create a new AudioEvent

   @param instrumentAudio    to of event in
   @param position of event
   @param name     of event
   @return new audio event
   */
  public static InstrumentAudioEvent create(InstrumentAudio instrumentAudio, double position, String name) {
    return create()
      .setInstrumentId(instrumentAudio.getInstrumentId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setPosition(position)
      .setName(name);
  }

  /**
   Create a new AudioEvent

   @return new audio event
   */
  public static InstrumentAudioEvent create() {
    return (InstrumentAudioEvent) new InstrumentAudioEvent().setId(UUID.randomUUID());
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
  public InstrumentAudioEvent setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public UUID getParentId() {
    return instrumentId;
  }

  /**
   get AudioId

   @return AudioId
   */
  public UUID getInstrumentAudioId() {
    return instrumentAudioId;
  }

  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  /**
   set Audio

   @param audio to set
   @return this AudioEvent (for chaining methods)
   */
  public InstrumentAudioEvent setAudio(InstrumentAudio audio) {
    return setInstrumentAudioId(audio.getId());
  }

  /**
   set AudioId

   @param instrumentAudioId to set
   @return this AudioEvent (for chaining methods)
   */
  public InstrumentAudioEvent setInstrumentAudioId(UUID instrumentAudioId) {
    this.instrumentAudioId = instrumentAudioId;
    return this;
  }

  @Override
  public InstrumentAudioEvent setDuration(Double duration) {
    super.setDuration(duration);
    return this;
  }

  public InstrumentAudioEvent setName(String name) {
    this.name = Text.toUpperSlug(name);
    return this;
  }

  @Override
  public InstrumentAudioEvent setNote(String note) {
    super.setNote(note);
    return this;
  }

  @Override
  public InstrumentAudioEvent setPosition(Double position) {
    super.setPosition(position);
    return this;
  }


  @Override
  public InstrumentAudioEvent setVelocity(Double velocity) {
    super.setVelocity(velocity);
    return this;
  }

  @Override
  public void validate() throws CoreException {
    require(instrumentId, "Instrument ID");
    require(instrumentAudioId, "Audio ID");

    require(name, "Name");

    EventEntity.validate(this);
  }
}
