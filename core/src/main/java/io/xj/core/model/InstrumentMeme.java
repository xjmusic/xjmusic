// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.MemeEntity;
import io.xj.core.exception.CoreException;
import io.xj.core.util.Text;

import java.util.UUID;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class InstrumentMeme extends MemeEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(MemeEntity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Instrument.class)
    .build();
  private UUID instrumentId;

  /**
   Create a new instrument meme

   @return meme
   */
  public static InstrumentMeme create() {
    return (InstrumentMeme) new InstrumentMeme().setId(UUID.randomUUID());
  }

  /**
   Create a new instrument meme

   @param name of meme
   @return meme
   */
  public static InstrumentMeme create(Instrument instrument, String name) {
    return create()
      .setInstrumentId(instrument.getId())
      .setName(name);
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
  public InstrumentMeme setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public UUID getParentId() {
    return instrumentId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public InstrumentMeme setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public void validate() throws CoreException {
    require(instrumentId, "Instrument ID");

    MemeEntity.validate(this);
  }

}
