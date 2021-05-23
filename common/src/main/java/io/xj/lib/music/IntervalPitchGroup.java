// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

/**
 Interval Pitch Group is the super-entity to a Key, Chord or Scale- any group of pitches at specific intervals.
 */
public abstract class IntervalPitchGroup {
  protected String description;
  protected String name;
  // Root Pitch Class
  protected PitchClass root;
  // Slash Root Pitch Class
  protected PitchClass slashRoot;
  // the (flat/sharp) adjustment symbol, which will be used to express this chord
  protected AdjSymbol adjSymbol;
  // a map of this chord's +/- semitones-from-root, for each interval
  protected SortedMap<Interval, Integer> tones;

  public IntervalPitchGroup(String name) {

    // initialize tones map
    tones = Maps.newTreeMap();

    // Don't set values if there's nothing to set
    if (Objects.isNull(name) || name.length() == 0)
      return;

    // store original name
    this.name = name;

    // determine whether the name is "sharps" or "flats"
    adjSymbol = AdjSymbol.of(name);

    // Root utility separates root from remaining text
    Root root = Root.of(name);

    // parse the root, and keep the remaining string
    this.root = root.getPitchClass();

    // parse the slash root
    this.slashRoot = SlashRoot.of(name).orDefault(this.root);

    // description is everything AFTER the root, in the original name
    description = root.getRemainingText();

    // parse the chord Form
    parseSchema(root.getRemainingText());
  }

  public static String detailsOf(Map<Interval, PitchClass> intervalPitchClasses, AdjSymbol adjSymbol) {
    int i = 0;
    String[] detailStrings = new String[intervalPitchClasses.size()];
    for (Interval interval : Interval.values()) {
      if (intervalPitchClasses.containsKey(interval)) {
        detailStrings[i] = interval.getValue() + ":" + intervalPitchClasses.get(interval).toString(adjSymbol);
        i++;
      }
    }
    return "{ " + String.join(", ", detailStrings) + " }";
  }

  /**
   Get the full description of the chord, including the root and the remaining description thereafter

   @return full description
   */
  public String getFullDescription() {
    return root.toString(adjSymbol) + " " + description;
  }

  public String getOriginalDescription() {
    return description;
  }

  /**
   Classes must implement this method

   @param text to parse schema from
   */
  protected abstract void parseSchema(String text);

  /**
   Notes to obtain the notes from the Chord, in a particular octave

   @param octave to get notes in
   @return notes
   */
  public List<Note> toNotes(int octave) {
    List<Note> notes = Lists.newArrayList();
    Note rootNote = Note.of(root, octave);
    tones.forEach((interval, offsetSemitones) ->
      notes.add(rootNote.shift(offsetSemitones)));
    return notes;
  }

  /**
   Detailed String expression of interval pitch group

   @return chord as string
   */
  public String details() {
    return "'" + name + "'" + " " + "(" +
      root.toString(adjSymbol) + ":" + " " +
      String.join(", ", toneOffsetStrings()) + ")";
  }

  /**
   @return pitch classes at intervals
   */
  public Map<Interval, PitchClass> getPitchClasses() {
    Map<Interval, PitchClass> pitchClasses = Maps.newHashMap();
    tones.forEach((interval, offsetSemitones) ->
      pitchClasses.put(interval, root.step(offsetSemitones).getPitchClass()));
    return pitchClasses;
  }

  /**
   String expression of interval pitch group, original name

   @return scale as string
   */
  public String toString() {
    return name;
  }

  /**
   Delta to another Key calculated in +/- semitones

   @param target key to calculate delta to
   @return delta +/- semitones to another key
   */
  public int delta(IntervalPitchGroup target) {
    return root.delta(target.getRoot());
  }

  public String getName() {
    return name;
  }

  public PitchClass getRoot() {
    return root;
  }

  /**
   [#176728338] XJ understands the root of a slash chord
   */
  public PitchClass getSlashRoot() {
    return slashRoot;
  }

  public SortedMap<Interval, Integer> getTones() {
    return tones;
  }

  public AdjSymbol getAdjSymbol() {
    return adjSymbol;
  }

  /**
   Array of tone offsets

   @return tone offsets
   */
  private String[] toneOffsetStrings() {
    int i = 0;
    Object[] offsets = tones.values().toArray();
    Arrays.sort(offsets);
    String[] offsetStrings = new String[offsets.length];
    for (Object toneOffset : offsets) {
      offsetStrings[i] = String.valueOf(toneOffset);
      i++;
    }
    return offsetStrings;
  }

}
