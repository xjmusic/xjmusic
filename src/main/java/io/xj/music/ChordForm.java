// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import io.xj.music.Interval;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 Chords have different Forms, such as Triad, Seventh, Extended, Added/Omitted, Specific or General.
 <p>
 Form is identified by positive/negative regular expressions, and then adds/removes pitch classes by interval from the root of the chord.
 */
public class ChordForm {
  private String name;
  private Pattern search;
  private Map<Interval, Integer> add;
  private List<Interval> omit;

  ChordForm(String name, Pattern search, Map<Interval, Integer> add, List<Interval> omit) {
    this.name = name;
    this.search = search;
    this.add = Objects.isNull(add) ? ImmutableMap.of() : add;
    this.omit = Objects.isNull(omit) ? ImmutableList.of() : omit;
  }

  /**
   Get the name of this form

   @return name
   */
  public String getName() {
    return name;
  }

  /**
   Get the search (regex) expression for this form

   @return regex pattern
   */
  public Pattern getSearch() {
    return search;
  }

  /**
   maps an interval-from-chord-root to a +/1 semitone adjustment

   @return map of tones to add at intervals
   */
  public Map<Interval, Integer> getAdd() {
    return add;
  }

  /**
   list of intervals-from-chord-root to omit

   @return list of tones to omit
   */
  public List<Interval> getOmit() {
    return omit;
  }

  /**
   in processes the positive/negative regular expressions to determine if this form matches a string.

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
