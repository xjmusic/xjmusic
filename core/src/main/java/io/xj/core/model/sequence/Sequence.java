// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.util.Text;

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
public class Sequence extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "sequence";
  public static final String KEY_MANY = "sequences";

  private String name;
  private String _type; // to hold value before validation
  private SequenceType type;
  private BigInteger libraryId;
  private BigInteger userId;
  private String key;
  private Double density;
  private Double tempo;
  private SequenceState state;
  private String _stateString; // pending validation, copied to `state` field


  public Sequence() {}

  public Sequence(BigInteger id) {
    this.id = id;
  }

  public Sequence(int id) {
    this.id = BigInteger.valueOf((long) id);
  }

  public String getName() {
    return name;
  }

  public Sequence setName(String value) {
    name = value;
    return this;
  }

  public SequenceState getState() {
    return state;
  }

  /**
   This sets the state String, however the value will remain null
   until validate() is called and the value is cast to enum

   @param stateString pending validation
   */
  public Sequence setState(String stateString) {
    _stateString = Text.toAlphabetical(stateString);
    return this;
  }

  public Sequence setStateEnum(SequenceState state) {
    this.state = state;
    return this;
  }


  public SequenceType getType() {
    return type;
  }

  public Sequence setType(String value) {
    _type = value;
    return this;
  }

  public void setTypeEnum(SequenceType type) {
    this.type = type;
  }

  public BigInteger getLibraryId() {
    return libraryId;
  }

  public Sequence setLibraryId(BigInteger value) {
    libraryId = value;
    return this;
  }

  public BigInteger getUserId() {
    return userId;
  }

  public Sequence setUserId(BigInteger value) {
    userId = value;
    return this;
  }

  public String getKey() {
    return key;
  }

  public Sequence setKey(String value) {
    key = value;
    return this;
  }

  public Double getDensity() {
    return density;
  }

  public Sequence setDensity(Double value) {
    density = value;
    return this;
  }

  public Double getTempo() {
    return tempo;
  }

  public Sequence setTempo(Double value) {
    tempo = value;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return libraryId;
  }

  @Override
  public void validate() throws BusinessException {
    // throws its own BusinessException on failure
    if (Objects.isNull(type))
      type = SequenceType.validate(_type);

    // throws its own BusinessException on failure
    if (Objects.isNull(state))
      state = SequenceState.validate(_stateString);

    if (Objects.isNull(name) || name.isEmpty())
      throw new BusinessException("Name is required.");

    if (Objects.isNull(libraryId))
      throw new BusinessException("Library ID is required.");

    if (Objects.isNull(userId))
      throw new BusinessException("User ID is required.");

    if (Objects.isNull(type))
      throw new BusinessException("Type is required.");

    if (Objects.isNull(key) || key.isEmpty())
      throw new BusinessException("Key is required.");

    if (Objects.isNull(density))
      throw new BusinessException("Density is required.");

    if (Objects.isNull(tempo))
      throw new BusinessException("Tempo is required.");
  }

  @Override
  public String toString() {
    return name + " " + "(" + type + ")";
  }

}
