// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

/**
 In-memory cache of ingest of a chord usage in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestChordProgressionItem {
  static final Comparator<? super DigestChordProgressionItem> byUsageTimesLengthDescending = (o1, o2) -> Integer.compare(o2.computeUsageTimesLength(), o1.computeUsageTimesLength());
  private final ChordProgression chordProgression;
  private final Collection<SequenceChordProgression> usages = Lists.newArrayList();

  /**
   of instance

   @param chordProgression of chord
   */
  DigestChordProgressionItem(ChordProgression chordProgression) {
    this.chordProgression = chordProgression;
  }

  /**
   Get chord progressions

   @return chord progressions
   */
  Collection<SequenceChordProgression> getUsages() {
    return Collections.unmodifiableCollection(usages);
  }

  /**
   Get chord progression

   @return chord progression
   */
  public ChordProgression getChordProgression() {
    return chordProgression;
  }

  /**
   Get chord descriptor length in # of segments

   @return # of segments in descriptor
   */
  int getDescriptorLength() {
    return chordProgression.size();
  }

  /**
   Add a ChordEntity progression for this descriptor

   @param chordProgression to add
   */
  public void add(SequenceChordProgression chordProgression) {
    usages.add(chordProgression);
  }

  /**
   Add a ChordEntity progression for this descriptor, only if we don't already have one for that parent id

   @param candidate to maybe add
   */
  void addIfUniqueParent(SequenceChordProgression candidate) {
    for (SequenceChordProgression existing : usages)
      if (Objects.equal(existing.getParentId(), candidate.getParentId()))
        return;

    usages.add(candidate);
  }

  /**
   Get max: usages or descriptor length

   @return max: usages or descriptor length
   */
  private int computeUsageTimesLength() {
    return getDescriptorLength() * computeUniqueUsages();
  }

  /**
   Compute unique usages of chord progression (different parent ids

   @return count of unique usages
   */
  private int computeUniqueUsages() {
    Map<UUID, Boolean> search = Maps.newHashMap();
    getUsages().forEach(chordProgression ->
      search.put(chordProgression.getParentId(), true)
    );
    return search.keySet().size();
  }

}
