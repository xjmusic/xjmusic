// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import java.math.BigInteger;
import java.util.Objects;

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
public class Instrument extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "instrument";
  public static final String KEY_MANY = "instruments";

  private String description;
  private String _type; // to hold value before validation
  private InstrumentType type;
  private BigInteger libraryId;
  private BigInteger userId;
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

  public void setTypeEnum(InstrumentType type) {
    this.type = type;
  }

  public BigInteger getLibraryId() {
    return libraryId;
  }

  public Instrument setLibraryId(BigInteger value) {
    libraryId = value;
    return this;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public Instrument setUserId(BigInteger value) {
    userId = value;
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
    if (Objects.isNull(type))
      type = InstrumentType.validate(_type);

    if (Objects.isNull(libraryId))
      throw new BusinessException("Library ID is required.");

    if (Objects.isNull(userId))
      throw new BusinessException("User ID is required.");

    if (Objects.isNull(type))
      throw new BusinessException("Type is required.");

    if (Objects.isNull(description) || description.isEmpty())
      throw new BusinessException("Description is required.");

    if (Objects.isNull(density))
      throw new BusinessException("Density is required.");

  }

  @Override
  public String toString() {
    return description + " " + "(" + type + ")";
  }

}
