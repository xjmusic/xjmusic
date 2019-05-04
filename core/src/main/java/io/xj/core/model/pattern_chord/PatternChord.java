// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_chord;

import io.xj.core.exception.CoreException;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.util.Value;

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
public class PatternChord extends EntityImpl implements Chord<PatternChord> {
  public static final String KEY_ONE = "patternChord";
  public static final String KEY_MANY = "patternChords";
  private Double position;
  private String name;

  private BigInteger patternId;

  /**
   Empty constructor
   */
  public PatternChord() {
  }

  /**
   Constructor

   @param patternId pattern to which chord belongs
   @param position  of chord in pattern, starting at 0
   @param name      of chord
   */
  public PatternChord(BigInteger patternId, Double position, String name) {
    this.patternId = patternId;
    this.position = Value.limitFloatingPointPlaces(position);
    this.name = name;
  }

  public static PatternChord of(String name) {
    return new PatternChord().setName(name);
  }

  public BigInteger getPatternId() {
    return patternId;
  }

  public PatternChord setPatternId(BigInteger patternId) {
    this.patternId = patternId;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    Chord.validate(this);
    if (Objects.isNull(patternId))
      throw new CoreException("Pattern ID is required.");
  }

  @Override
  public BigInteger getParentId() {
    return patternId;
  }

  @Override
  public PatternChord setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public PatternChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }

}
