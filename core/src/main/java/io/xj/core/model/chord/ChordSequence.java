// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chord;

import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.phase_chord.PhaseChord;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

//import io.xj.music.PitchClass;

/**
 Sequence of chords
 likely related to [#154234716] analysis of library contents includes sequences of chords.
 */
public class ChordSequence {
  public static final int MAX_SEMITONES = 12;
  private static final int MAX_DESCRIPTOR_CAPACITY = 4096;
  private static final String SEPARATOR_PRIMARY = ":";
  private static final String SEPARATOR_SECONDARY = "|";
  private static final String NOTHING = "";
  private static final Pattern NON_CHORD_DESCRIPTOR = Pattern.compile("[^a-zA-Z0-9+\\-#♭ ]");
  private final List<? extends Chord> chords;
  private final Chord rootChord;
  //  private final PitchClass rootChordPitchClass;
  private final BigInteger parentId;

  //
  public ChordSequence(BigInteger parentId, List<? extends Chord> chords) {
    this.parentId = parentId;
    this.chords = Lists.newArrayList(chords);
    rootChord = chords.get(0);
//    rootChordPitchClass = rootChord.toMusical().getRootPitchClass();
  }

  /**
   Compute descriptor

   @param chord to compute descriptor of
   @return descriptor
   */
  private static String formOf(Chord chord) {
    return NON_CHORD_DESCRIPTOR.matcher(chord.toMusical().colloquialFormName())
      .replaceAll(NOTHING);
  }

  /**
   Split a descriptor string into an array of descriptors

   @param descriptor to split
   @return array of segment descriptors
   */
  public static List<String> splitDescriptor(String descriptor) {
    return Lists.newArrayList(descriptor.split(SEPARATOR_PRIMARY));
  }

  /**
   In order to be certain that the needle is a redundant subset of descriptor X
   we must know that a continuous series of major segments are completely equal

   @param needle              descriptor to check for redundancy
   @param haystack            to search within
   @param redundancyThreshold max # of segments difference, that a subset be considered redundant. For example, at threshold of 2, a 3-length subset may be redundant of a 5-length, however a 2-length cannot be considered redundant of a 5-length.
   @return true if redundant
   */
  public static boolean isRedundantSubsetOfDescriptor(String needle, String haystack, int redundancyThreshold) {
    // Aren't considered redundant if they are equal
    if (Objects.equals(needle, haystack)) return false;

    // Can't be redundant if the haystack is shorter than the needle
    if (haystack.length() < needle.length()) return false;

    // Split the descriptors into segments
    List<String> needleSegments = splitDescriptor(needle);
    List<String> haystackSegments = splitDescriptor(haystack);
    int needleSize = needleSegments.size();
    int haystackSize = haystackSegments.size();

    // Can't be redundant if the needle has greater than or equal to number of segments as haystack
    if (needleSize >= haystackSize) return false;

    // Can't be redundant if the needle has greater than or equal to number of segments as haystack
    if (haystackSize > needleSize + redundancyThreshold) return false;

    // iterate through the most obvious possible match and return true if it hits
    boolean match = true;
    for (int n = 0; n < needleSize; n++) {
      if (!Objects.equals(needleSegments.get(n), haystackSegments.get(n))) match = false;
    }
    if (match) return true;

    // iterate through other possible matches and return true if any of them hit all the way through
    for (int h = 0; h < haystackSize - needleSize; h++) {
      // it's a possible match if the first haystack segment ends in the secondary separator plus the needle segment
      if (haystackSegments.get(h).endsWith(SEPARATOR_SECONDARY + needleSegments.get(0))) {
        match = true;
        for (int n = 1; n < needleSize; n++) {
          if (!Objects.equals(needleSegments.get(n), haystackSegments.get(h + n))) match = false;
        }
        if (match) return true;
      }
    }

    // if we made it here, there was no match
    return false;
  }

  /**
   Compute Δ semitones modulo n from root chord to add chord

   @param addChord compute Δ to
   @return Δ semitones modulo n (where n = max # of semitones, probably 12)
   */
  private static int deltaSemitonesModulo(Chord prevChord, Chord addChord) {
    return Math.floorMod(prevChord.toMusical().getRootPitchClass().delta(addChord.toMusical().getRootPitchClass()), MAX_SEMITONES);
  }

  /**
   Get parent id

   @return id
   */
  public BigInteger getParentId() {
    return parentId;
  }

  /**
   Get a list of all chords in the sequence

   @return list of chords
   */
  public List<? extends Chord> getChords() {
    return Collections.unmodifiableList(chords);
  }

  /*
   Compute Δ from root chord to add chord

   @param addChord compute Δ to
   @return Δ beats
   *
  private int deltaBeats(Chord addChord) {
    return addChord.getPosition() - rootChord.getPosition();
  }
  */

  /**
   compute the standard descriptor for this sequence of chords

   @return standard descriptor
   */
  public String getDescriptor() {
    if (Objects.isNull(chords) || chords.isEmpty()) return "";
    StringBuilder result = new StringBuilder(MAX_DESCRIPTOR_CAPACITY);

    // A. append form of chord 1
    result.append(formOf(rootChord));

    // repeat steps B* for chord 2...n (if present)
    int size = chords.size();
    if (1 < size) for (int i = 1; i < size; i++) {
      Chord addChord = chords.get(i);
      Chord prevChord = chords.get(i - 1);

      // Ba. append primary separator + Cn Δ semitones (modulo)
      result.append(SEPARATOR_PRIMARY);
      result.append(String.format("%d", deltaSemitonesModulo(prevChord, addChord)));

      // Bb. append secondary separator + append Cn form
      result.append(SEPARATOR_SECONDARY);
      result.append(formOf(addChord));

    }
    return result.toString();
  }

  /**
   Get class of chords in sequence.
   Note: checks only the first chord of the sequence,
   and assumes that all chords present will be of that same class

   @return class of chords
   */
  public ChordSequenceType getType() {
    if (chords.isEmpty()) return ChordSequenceType.Unknown;
    Class chordClass = chords.get(0).getClass();
    if (Objects.equals(chordClass, PhaseChord.class)) return ChordSequenceType.PhaseChordSequence;
    if (Objects.equals(chordClass, AudioChord.class)) return ChordSequenceType.AudioChordSequence;
    return ChordSequenceType.Unknown;
  }
}
