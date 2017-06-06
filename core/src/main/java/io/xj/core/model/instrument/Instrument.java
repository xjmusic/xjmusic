// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;
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
  public final static String PERCUSSIVE = "percussive";
  public final static String HARMONIC = "harmonic";
  public final static String MELODIC = "melodic";
  public final static String VOCAL = "vocal";

  public final static List<String> TYPES = ImmutableList.of(
    PERCUSSIVE,
    HARMONIC,
    MELODIC,
    VOCAL
  );
  /**
   For use in maps.
   */
  public static final String KEY_ONE = "instrument";
  public static final String KEY_MANY = "instruments";
  // Description
  private String description;
  // Type
  private String type;
  // Library
  private ULong libraryId;
  // User
  private ULong userId;
  // Density
  private Double density;

  public String getDescription() {
    return description;
  }

  public Instrument setDescription(String description) {
    this.description = description;
    return this;
  }

  public String getType() {
    return type;
  }

  public Instrument setType(String type) {
    this.type = Text.LowerSlug(type);
    return this;
  }

  public ULong getLibraryId() {
    return libraryId;
  }

  public Instrument setLibraryId(BigInteger libraryId) {
    this.libraryId = ULong.valueOf(libraryId);
    return this;
  }

  public ULong getUserId() {
    return userId;
  }

  public Instrument setUserId(BigInteger userId) {
    this.userId = ULong.valueOf(userId);
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Instrument setDensity(Double density) {
    this.density = density;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.libraryId == null) {
      throw new BusinessException("Library ID is required.");
    }
    if (this.userId == null) {
      throw new BusinessException("User ID is required.");
    }
    if (this.type == null || this.type.length() == 0) {
      throw new BusinessException("Type is required.");
    }
    if (!TYPES.contains(this.type)) {
      throw new BusinessException("'" + this.type + "' is not a valid type (" + CSV.join(TYPES) + ").");
    }
    if (this.description == null || this.description.length() == 0) {
      throw new BusinessException("Description is required.");
    }
    if (this.density == null) {
      throw new BusinessException("Density is required.");
    }
  }

  @Override
  public Instrument setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(INSTRUMENT.ID);
    description = record.get(INSTRUMENT.DESCRIPTION);
    libraryId = record.get(INSTRUMENT.LIBRARY_ID);
    userId = record.get(INSTRUMENT.USER_ID);
    type = record.get(INSTRUMENT.TYPE);
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
