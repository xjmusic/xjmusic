// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.INSTRUMENT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Instrument extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "instrument";
  public static final String KEY_MANY = "instruments";

  private String description;
  private String _type; // to hold value before validation
  private InstrumentType type;
  private ULong libraryId;
  private ULong userId;
  private Double density;

  public String getDescription() {
    return description;
  }

  public Instrument setDescription(String value) {
    description = value;
    return this;
  }

  public InstrumentType getType() {
    return type;
  }

  public Instrument setType(String value) {
    _type = value;
    return this;
  }

  public ULong getLibraryId() {
    return libraryId;
  }

  public Instrument setLibraryId(BigInteger value) {
    libraryId = ULong.valueOf(value);
    return this;
  }

  public ULong getUserId() {
    return userId;
  }

  public Instrument setUserId(BigInteger value) {
    userId = ULong.valueOf(value);
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Instrument setDensity(Double value) {
    density = value;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    type = InstrumentType.validate(_type);

    if (Objects.isNull(libraryId)) {
      throw new BusinessException("Library ID is required.");
    }
    if (Objects.isNull(userId)) {
      throw new BusinessException("User ID is required.");
    }
    if (Objects.isNull(type)) {
      throw new BusinessException("Type is required.");
    }
    if (Objects.isNull(description ) || description.isEmpty()) {
      throw new BusinessException("Description is required.");
    }
    if (Objects.isNull(density ) ) {
      throw new BusinessException("Density is required.");
    }
  }

  @Override
  public Instrument setFromRecord(Record record) throws BusinessException {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(INSTRUMENT.ID);
    description = record.get(INSTRUMENT.DESCRIPTION);
    libraryId = record.get(INSTRUMENT.LIBRARY_ID);
    userId = record.get(INSTRUMENT.USER_ID);
    type = InstrumentType.validate(record.get(INSTRUMENT.TYPE));
    density = record.get(INSTRUMENT.DENSITY);
    createdAt = record.get(INSTRUMENT.CREATED_AT);
    updatedAt = record.get(INSTRUMENT.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(INSTRUMENT.DESCRIPTION, description);
    fieldValues.put(INSTRUMENT.LIBRARY_ID, libraryId);
    fieldValues.put(INSTRUMENT.USER_ID, userId);
    fieldValues.put(INSTRUMENT.TYPE, type);
    fieldValues.put(INSTRUMENT.DENSITY, density);
    return fieldValues;
  }

}
