// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

import java.util.Comparator;
import java.util.Objects;

/**
 This represents common properties of all chords,
 although a Chord only actually exists as a Link Chord, Phase Chord, etc.
 */
public abstract class Chord extends Entity {
  public static final String KEY_ONE = "chord";
  public static final String KEY_MANY = "chords";
  public static final String SEPARATOR_DESCRIPTOR = ":";
  public static final String SEPARATOR_DESCRIPTOR_UNIT = "|";
  public static final String MARKER_NON_CHORD = "---";
  public static final Comparator<? super Chord> byPositionAscending = (Comparator<? super Chord>) (o1, o2) -> {
    if (o1.getPosition() > o2.getPosition()) return 1;
    if (o1.getPosition() < o2.getPosition()) return -1;
    return 0;
  };
  public static final Comparator<? super Chord> byPositionDescending = (Comparator<? super Chord>) (o1, o2) -> {
    if (o1.getPosition() > o2.getPosition()) return -1;
    if (o1.getPosition() < o2.getPosition()) return 1;
    return 0;
  };
  protected String name;
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
    if (Objects.isNull(name) || name.isEmpty()) {
      throw new BusinessException("Name is required.");
    }
    if (Objects.isNull(position)) {
      throw new BusinessException("Position is required.");
    }
  }

  public abstract Chord of(String name);

  /**
   Returns a musical chord of the current entity, for music related operations

   @return musical chord
   */
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  /**
   String of Chord

   @return string
   */
  public String toString() {
    return name + "@" + position;
  }

}
