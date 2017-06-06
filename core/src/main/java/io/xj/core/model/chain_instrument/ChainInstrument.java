// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chain_instrument;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHAIN_INSTRUMENT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainInstrument extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainInstrument";
  public static final String KEY_MANY = "chainInstruments";
  // Chain ID
  private ULong chainId;
  // Instrument ID
  private ULong instrumentId;

  public ULong getChainId() {
    return chainId;
  }

  public ChainInstrument setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public ULong getInstrumentId() {
    return instrumentId;
  }

  public ChainInstrument setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = ULong.valueOf(instrumentId);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.instrumentId == null) {
      throw new BusinessException("Instrument ID is required.");
    }
  }

  @Override
  public ChainInstrument setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN_INSTRUMENT.ID);
    chainId = record.get(CHAIN_INSTRUMENT.CHAIN_ID);
    instrumentId = record.get(CHAIN_INSTRUMENT.INSTRUMENT_ID);
    createdAt = record.get(CHAIN_INSTRUMENT.CREATED_AT);
    updatedAt = record.get(CHAIN_INSTRUMENT.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_INSTRUMENT.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_INSTRUMENT.INSTRUMENT_ID, instrumentId);
    return fieldValues;
  }


}
