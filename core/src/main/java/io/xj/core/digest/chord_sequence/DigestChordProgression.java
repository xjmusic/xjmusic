// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.digest.chord_sequence;

import io.xj.core.digest.Digest;
import io.xj.core.digest.chord_sequence.impl.DigestChordProgressionItem;

import java.util.List;
import java.util.Map;

public interface DigestChordProgression extends Digest {

  /**
   Get the underlying evaluated sequence map, a map of unique descriptor strings (complete set from evaluated entities) to their digested chords.

   @return map of descriptor strings to digested chords
   */
  Map<String, DigestChordProgressionItem> getEvaluatedSequenceMap();

  /**
   Get sorted descriptors
   @return list of descriptors, sorted by
   */
  List<String> getSortedDescriptors();

}
