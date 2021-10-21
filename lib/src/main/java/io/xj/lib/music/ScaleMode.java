// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 Mode is identified by positive/negative regular expressions, and then adds/removes pitch classes by interval from the root of the scale.
 <p>
 Scales have different Modes, such as Locrian, Ionian, Natural, Melodic Minor or Augmented Phrygian.
 */
public class ScaleMode {
  private final String name;
  private final Pattern search;
  private final List<Integer> deltas;
  private final List<Interval> omit;

  ScaleMode(String name, Pattern search, List<Integer> deltas, List<Interval> omit) {
    this.name = name;
    this.search = search;
    this.deltas = Objects.isNull(deltas) ? ImmutableList.of() : deltas;
    this.omit = Objects.isNull(omit) ? ImmutableList.of() : omit;
  }

  /**
   Get the name of this mode

   @return name
   */
  public String getName() {
    return name;
  }

  /**
   Get the search (regex) expression for this mode

   @return regex pattern
   */
  public Pattern getSearch() {
    return search;
  }

  /**
   list of deltas (# semitones) from the root of the scale up

   @return map of tones to deltas at deltas
   */
  public List<Integer> getDeltas() {
    return deltas;
  }

  /**
   list of deltas (e.g. "Third", "Fifth") to omit from the scale

   @return list of tones to omit
   */
  public List<Interval> getOmit() {
    return omit;
  }

  /**
   in processes the positive/negative regular expressions to determine if this mode matches a string.

   @param toMatch string to process
   @return whether it matches
   */
  public boolean in(String toMatch) {
    return Objects.isNull(this.search) || this.search.matcher(toMatch).find();
    //if this.neg != nil {
    //	if this.neg.in(s) {
    //		return false
    //	}
    //}
  }

}
