// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.chord;

import io.xj.core.model.chord.Chord;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ChordProgression {
  private static final double NO_SCORE = 0.0;
  private final List<ChordNode> chordNodes;

  /**
   Construct empty chord progression.
   */
  public ChordProgression(List<ChordNode> chordNodes) {
    this.chordNodes = Lists.newArrayList(chordNodes);
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

  /**
   Splice a forward and reverse chord progression at a specified index

   @param target      chord progression
   @param size        of final chord progression
   @param spliceIndex at which to splice
   @return spliced final chord progression
   */
  public ChordProgression splice(ChordProgression target, int size, int spliceIndex) {
    int endIndex = size - 1;
    int closingEndIndex = target.size() - 1;

    List<ChordNode> resultNodes = Lists.newArrayList();
    for (int idx = 0; idx < size; idx++) {
      if (idx < spliceIndex)
        resultNodes.add(getChordNodes().get(idx));
      else
        resultNodes.add(target.getChordNodes().get(closingEndIndex - (endIndex - idx)));
    }

    return new ChordProgression(resultNodes);
  }

  /**
   Search for a point at which forward-reverse collide, between a forward-generated chord progression and a reverse-generated chord progression into one final progression, which begins in the forward-generated chords and ends with the reverse-generated chords.
   - score all possible combinations of phase total and splice point. Any pair of phase total and splice point determines one potential outcome.

   @param target chord progression to consolidate
   @return final progression, which begins in the forward-generated chords and ends with the reverse-generated chords.
   */
  public ChordProgression spliceAtCollision(ChordProgression target, Set<Integer> sizes, Integer safetyMargin) {
    double highScore = NO_SCORE;
    int bestSize = 0;
    int bestSpliceIndex = 0;
    for (int size : sizes)
      for (int spliceIndex = safetyMargin; spliceIndex < size - safetyMargin; spliceIndex++) {
        double score = scorePotentialSplice(target, size, spliceIndex);
        if (score > highScore) {
          highScore = score;
          bestSize = size;
          bestSpliceIndex = spliceIndex;
        }
      }

    return splice(target, bestSize, bestSpliceIndex);
  }

  /**
   Score a potential splice of opening and closing walks
   - determine if this splice would even be possible-- the closing walk needs to have enough chords to make it to the end of the size from the splice index, and the opening walk needs to have enough chords to make ti to the splice index
   - score the possible splicing of these two walks at this size and splice index
   - Since there may be multiple opportunities to splice on a matching chord even, let's score the splice point chord AND the 50% of the following chord (or something like that!)

   @param target      chord progression to (score) splice
   @param finalSize   of resulting progression
   @param spliceIndex index to switch from the opening walk to the closing walk
   @return 0 if impossible, score above zero if anywhere from possible to optimal.
   */
  public Double scorePotentialSplice(ChordProgression target, Integer finalSize, Integer spliceIndex) {
    int endIndex = finalSize - 1;
    int closingSpliceIndex = target.size() - 1 - (endIndex - spliceIndex);

    if (0 > spliceIndex) return NO_SCORE;
    if (0 > closingSpliceIndex) return NO_SCORE;
    if (endIndex < spliceIndex) return NO_SCORE;
    if (size() <= spliceIndex + 1) return NO_SCORE;
    if (target.size() <= closingSpliceIndex + 1) return NO_SCORE;

    return
      getChordNodes().get(spliceIndex).similarity(target.getChordNodes().get(closingSpliceIndex))
        + getChordNodes().get(spliceIndex + 1).similarity(target.getChordNodes().get(closingSpliceIndex + 1)) / 2;
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
   @return reversed copy of this chord progression
   */
  public ChordProgression reversed() {
    return new ChordProgression(Lists.reverse(chordNodes));
  }

  /**
   Whether this chord progression has the same # of chords as another

   @param other to compare to
   @return true is has same # of chords.
   */
  boolean isEquivalent(ChordProgression other) {
    if (!Objects.equals(chordNodes.size(), other.getChordNodes().size()))
      return false;
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

}
