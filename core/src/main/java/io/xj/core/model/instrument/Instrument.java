// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.entity.impl.EntityImpl;

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
public class Instrument extends EntityImpl {
  public static final String KEY_ONE = "instrument";
  public static final String KEY_MANY = "instruments";

  private String description;
  private String _type; // to hold value before validation
  private InstrumentType type;
  private BigInteger libraryId;
  private BigInteger userId;
  private Double density;

  public Instrument() {}

  public Instrument(int id) {
    this.id = BigInteger.valueOf((long) id);
  }

  public Instrument(BigInteger id) {
    this.id = id;
  }


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
  public BigInteger getParentId() {
    return libraryId;
  }

  @Override
  public void validate() throws CoreException {
    // throws its own CoreException on failure
    if (Objects.isNull(type))
      type = InstrumentType.validate(_type);

    if (Objects.isNull(libraryId))
      throw new CoreException("Library ID is required.");

    if (Objects.isNull(userId))
      throw new CoreException("User ID is required.");

    if (Objects.isNull(type))
      throw new CoreException("Type is required.");

    if (Objects.isNull(description) || description.isEmpty())
      throw new CoreException("Description is required.");

    if (Objects.isNull(density))
      throw new CoreException("Density is required.");

  }

  @Override
  public String toString() {
    return description + " " + "(" + type + ")";
  }

}
