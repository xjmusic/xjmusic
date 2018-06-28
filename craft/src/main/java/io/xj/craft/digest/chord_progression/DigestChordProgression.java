// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.chord_progression;

import io.xj.craft.digest.Digest;
import io.xj.craft.digest.chord_progression.impl.DigestChordProgressionItem;

import java.util.List;
import java.util.Map;

public interface DigestChordProgression extends Digest {

  /**
   Get the underlying evaluated sequence map, a map of unique descriptor strings (complete set from evaluated entities) to their digested entities.

   @return map of descriptor strings to digested entities
   */
  Map<String, DigestChordProgressionItem> getEvaluatedSequenceMap();

  /**
   Get sorted descriptors
   @return list of descriptors, sorted by
   */
  List<String> getSortedDescriptors();

}
