// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

/**
 This represents common properties of all chords,
 although a Chord only actually exists as a Link Chord, Phase Chord, etc.
 */
public abstract class Chord extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chord";
  public static final String KEY_MANY = "chords";
  /**
   Name
   */
  protected String name;
  /**
   Position
   */
  protected Double position;

  public String getName() {
    return name;
  }

  public abstract Chord setName(String name);

  public Double getPosition() {
    return position;
  }

  public abstract Chord setPosition(Double position);

  @Override
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Name is required.");
    }
    if (this.position == null) {
      throw new BusinessException("Position is required.");
    }
  }

}
