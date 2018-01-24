// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chord;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChordProgression {
  private final List<ChordNode> chordNodes;

  /**
   Construct empty chord progression.
   */
  public ChordProgression(List<ChordNode> chordNodes) {
    this.chordNodes = chordNodes;
  }

  /**
   Construct chord progression reverse-engineered from the output string progression

   @param progression string to reverse-engineered into a chord progression
   */
  public ChordProgression(String progression) {
    chordNodes = Lists.newArrayList();
    List<String> pieces = Splitter.on(Chord.SEPARATOR_DESCRIPTOR).splitToList(progression);
    pieces.forEach(piece -> chordNodes.add(new ChordNode(piece)));
  }

  /**
   Get a list of chord descriptor units in this sequence

   @return list of chord descriptor units
   */
  public List<ChordNode> getChordNodes() {
    return Collections.unmodifiableList(chordNodes);
  }

  /**
   In order to be certain that the needle is a redundant subset of descriptor X
   we must know that a continuous series of major segments are completely equal

   @param needle              to test for being a redundant subset of this.
   @param redundancyThreshold max # of segments difference, that a subset be considered redundant. For example, at threshold of 2, a 3-length subset may be redundant of a 5-length, however a 2-length cannot be considered redundant of a 5-length.
   @return true if redundant
   */
  public Boolean isRedundantSubset(ChordProgression needle, Integer redundancyThreshold) {
    // Aren't considered redundant if they are equal
    if (Objects.equals(needle.getChordNodes(), chordNodes))
      return false;

    // Consider the segments
    List<ChordNode> needleUnits = needle.getChordNodes();
    int needleSize = needleUnits.size();
    int haystackSize = chordNodes.size();

    // Can't be redundant if the needle has greater than or equal to number of segments as haystack
    if (needleSize >= haystackSize) return false;

    // Can't be redundant if the needle has greater than or equal to number of segments as haystack
    if (haystackSize > needleSize + redundancyThreshold) return false;

    // iterate through other possible matches and return true if any of them hit all the way through
    for (int haystackOffset = 0; haystackOffset < haystackSize - needleSize; haystackOffset++) {
      if (isMatchingSubset(needleUnits, haystackOffset)) return true;
    }

    // if we made it here, there was no match
    return false;
  }

  /**
   Whether the provided is a matching subset in the haystack, beginning at the provided offset

   @param needle         to match
   @param haystackOffset in haystack to begin matching
   @return true if matches completely
   */
  private boolean isMatchingSubset(List<ChordNode> needle, int haystackOffset) {
    int size = needle.size();
    for (int n = 0; n < size; n++) {
      if (!needle.get(n).isEquivalentTo(chordNodes.get(haystackOffset + n)))
        return false;
    }
    return true;
  }

  /**
   compute the standard descriptor for this sequence of chords

   @return standard descriptor
   */
  @Override
  public String toString() {
    if (chordNodes.isEmpty()) return "";
    List<String> pieces = Lists.newArrayList();
    chordNodes.forEach(chordDescriptorUnit -> pieces.add(chordDescriptorUnit.toString()));
    return Joiner.on(Chord.SEPARATOR_DESCRIPTOR).join(pieces);
  }

  /**
   Get size of chord progression, in terms of # units

   @return # of units in chord progression
   */
  public int size() {
    return chordNodes.size();
  }

  /**
   Get whether the chord descriptor units are empty

   @return true if empty
   */
  public boolean isEmpty() {
    return chordNodes.isEmpty();
  }

  /**
   Add next Chord in progression

   @param next chord to add
   */
  public void add(ChordNode next) {
    chordNodes.add(next);
  }

  /**
   Construct a new chord progression from a list of chords

   @param chords to construct progression from
   @param <C>    type of chord
   @return chord progression
   */
  public static <C extends Chord> ChordProgression of(List<C> chords) {
    List<ChordNode> chordNodes = Lists.newArrayList();

    // A. append form of chord 1
    if (!chords.isEmpty())
      chordNodes.add(new ChordNode(chords.get(0)));

    // repeat steps B* for chord 2...n (if present)
    int size = chords.size();
    if (1 < size) for (int i = 1; i < size; i++) {
      Chord addChord = chords.get(i);
      Chord prevChord = chords.get(i - 1);
      chordNodes.add(new ChordNode(prevChord, addChord));
    }

    return new ChordProgression(chordNodes);
  }

}
