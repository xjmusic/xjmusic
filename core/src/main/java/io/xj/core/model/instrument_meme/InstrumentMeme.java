// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.instrument_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.MemeEntity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.INSTRUMENT_MEME;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class InstrumentMeme extends MemeEntity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "instrumentMeme";
  public static final String KEY_MANY = "instrumentMemes";
  // Instrument ID
  private ULong instrumentId;

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public InstrumentMeme setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
    return this;
  }

  public String getName() {
    return name;
  }

  public InstrumentMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
    super.validate();
  }

  @Override
  public InstrumentMeme setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(INSTRUMENT_MEME.ID);
    instrumentId = record.get(INSTRUMENT_MEME.INSTRUMENT_ID);
    name = record.get(INSTRUMENT_MEME.NAME);
    createdAt = record.get(INSTRUMENT_MEME.CREATED_AT);
    updatedAt = record.get(INSTRUMENT_MEME.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(INSTRUMENT_MEME.INSTRUMENT_ID, instrumentId);
    fieldValues.put(INSTRUMENT_MEME.NAME, name);
    return fieldValues;
  }


}
