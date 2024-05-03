// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.entity.common;

import io.xj.hub.entity.EntityUtils;
import io.xj.hub.util.ValueException;
import io.xj.hub.util.ValueUtils;

/**
 This represents common properties of all entities,
 although a ChordEntity only actually exists as a Segment ChordEntity, Pattern ChordEntity, etc.
 */
public class ChordEntity {
  String name;
  Double position;

  /**
   validation of common ChordEntity attributes

   @throws ValueException on invalid
   */
  public static void validate(Object chord) throws ValueException {
    try {
      ValueUtils.require(EntityUtils.get(chord, "name"), "Name");
      ValueUtils.require(EntityUtils.get(chord, "position"), "Position");
    } catch (Exception e) {
      throw new ValueException(e);
    }
  }

  /**
   Name

   @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   Set name

   @param name to set
   @return this (for chaining setters)
   */
  public ChordEntity setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Position

   @return position
   */
  public Double getPosition() {
    return this.position;
  }

  /**
   Set position

   @param position to set
   @return this (for chaining setters)
   */
  public ChordEntity setPosition(Double position) {
    this.position = position;
    return this;
  }


  @Override
  public String toString() {
    return name + "@" + position;
  }

}
