// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.MemeEntity;

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
  public InstrumentMeme setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public void validate() throws ValueException {
    Value.require(instrumentId, "Instrument ID");

    MemeEntity.validate(this);
  }

}
