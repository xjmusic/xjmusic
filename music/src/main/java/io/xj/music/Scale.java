// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.music;

import io.xj.music.schema.IntervalPitchGroup;
import io.xj.music.schema.ScaleMode;
import io.xj.music.schema.ScaleModes;

import java.util.Objects;
import java.util.SortedMap;

/**
 Scale in a particular key
 <p>
 In music theory, a scale is any set of musical notes ordered by fundamental frequency or pitch.
 <p>
 https://en.wikipedia.org/wiki/Scale_(music)
 <p>
 A scale ordered by increasing pitch is an ascending scale, and a scale ordered by decreasing pitch is a descending scale. Some scales contain different pitches when ascending than when descending. For example, the Melodic minor scale.
 */
public class Scale extends IntervalPitchGroup {
  private ScaleMode mode;

  /**
   Construct an empty Scale
   */
  public Scale() {
    super("");
  }

  /**
   Construct a named scale

   @param name of scale
   */
  private Scale(String name) {
    super(name);
  }

  /**
   Instantiate a scale in particular mode, e.g. of("C minor 7")

   @param name of scale
   @return scale
   */
  public static Scale of(String name) {
    return new Scale(name);
  }

  /**
   Copies this object to a new Note

   @return new note
   */
  private Scale copy() {
    return new Scale()
      .setRootPitchClass(root)
      .setOriginalDescription(getOriginalDescription())
      .setAdjSymbol(adjSymbol)
      .setTones(tones)
      .setMode(mode);
  }

  /**
   Transpose a scale +/- semitones
   */
  Scale transpose(int deltaSemitones) {
    return copy()
      .setAdjSymbol(adjSymbol)
      .setRootPitchClass(this.root.step(deltaSemitones).getPitchClass());
  }

  private Scale setOriginalDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   Build the scale by processing all Modes against the given name.

   @param text of scale
   */
  protected void parseSchema(String text) {
    ScaleModes.forEach(mode -> {
      if (mode.in(text)) {
        this.mode = mode;
      }
    });
    if (Objects.nonNull(mode)) {
      int offset = 0;
      int intervalNum = 1;
      setToneIfNotOmittedByMode(Interval.valueOf(intervalNum), offset, mode);
      for (Integer delta : mode.getDeltas()) {
        intervalNum++;
        offset += delta;
        setToneIfNotOmittedByMode(Interval.valueOf(intervalNum), offset, mode);
      }
    }
  }

  /**
   Set tone in map, if not omitted by the current mode

   @param interval to set
   @param offset   to set interval to
   @param mode     of scale
   */
  private void setToneIfNotOmittedByMode(Interval interval, Integer offset, ScaleMode mode) {
    if (!mode.getOmit().contains(interval))
      this.tones.put(interval, offset);
  }

  public Scale setRootPitchClass(PitchClass root) {
    this.root = root;
    return this;
  }

  public Scale setAdjSymbol(AdjSymbol adjSymbol) {
    this.adjSymbol = adjSymbol;
    return this;
  }

  public Scale setTones(SortedMap<Interval, Integer> tones) {
    this.tones = tones;
    return this;
  }

  public Scale setMode(ScaleMode mode) {
    this.mode = mode;
    return this;
  }

}
