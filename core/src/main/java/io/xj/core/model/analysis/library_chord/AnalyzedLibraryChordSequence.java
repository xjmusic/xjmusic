// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.analysis.library_chord;

import io.xj.core.model.chord.ChordSequence;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 In-memory cache of analysis of a chord usage in a library
 <p>
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class AnalyzedLibraryChordSequence {
  public static final Comparator<? super AnalyzedLibraryChordSequence> byUsageTimesLengthDescending = (o1, o2) -> Integer.compare(o2.computeUsageTimesLength(), o1.computeUsageTimesLength());
  private final String descriptor;
  private final Collection<ChordSequence> usages = Lists.newArrayList();

  /**
   New instance

   @param descriptor of chord
   */
  public AnalyzedLibraryChordSequence(String descriptor) {
    this.descriptor = descriptor;
  }

  /**
   Get chord sequences

   @return chord sequences
   */
  public Collection<ChordSequence> getUsages() {
    return Collections.unmodifiableCollection(usages);
  }

  /**
   Get chord sequence descriptor

   @return chord sequence descriptor
   */
  public String getDescriptor() {
    return descriptor;
  }

  /**
   Get chord descriptor length in # of segments

   @return # of segments in descriptor
   */
  public int getDescriptorLength() {
    return ChordSequence.splitDescriptor(descriptor).size();
  }

  /**
   Add a Chord Sequence for this descriptor

   @param chordSequence to add
   */
  public void add(ChordSequence chordSequence) {
    usages.add(chordSequence);
  }

  /**
   Add a Chord Sequence for this descriptor, only if we don't already have one for that parent id

   @param candidate to maybe add
   */
  public void addIfUniqueParent(ChordSequence candidate) {
    for (ChordSequence existing : usages)
      if (Objects.equal(existing.getParentId(), candidate.getParentId()))
        return;

    usages.add(candidate);
  }

  /**
   Get max: usages or descriptor length

   @return max: usages or descriptor length
   */
  public int computeUsageTimesLength() {
    return getDescriptorLength() * computeUniqueUsages();
  }

  /**
   Compute unique usages of chord sequence (different parent ids

   @return count of unique usages
   */
  private int computeUniqueUsages() {
    Map<BigInteger, Boolean> search = Maps.newConcurrentMap();
    getUsages().forEach(chordSequence ->
      search.put(chordSequence.getParentId(), true)
    );
    return search.keySet().size();
  }

}
