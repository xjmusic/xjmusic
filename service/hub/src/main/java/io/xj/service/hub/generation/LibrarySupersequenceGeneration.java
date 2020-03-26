// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import io.xj.service.hub.digest.ChordNode;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 [#154548999] Artist wants to generate a Library Supersequence in order to of a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.
 */
public interface LibrarySupersequenceGeneration extends Generation {

  /**
   Get a collection of the unique chord nodes covered by this generated library super sequence.

   @return collection of chord nodes
   */
  Collection<ChordNode> getCoveredNodes();

  /**
   Get generated patterns

   @return generated patterns
   */
  List<ProgramSequencePattern> getGeneratedPatterns();

  /**
   Get generated pattern entities.

   @return map of pattern id to list of generated pattern entities
   */
  Map<BigInteger, List<ProgramSequenceChord>> getGeneratedSequenceChords();

  /**
   Get the sequence that was provided as a target to generate supersequence around

   @return sequence
   */
  ProgramSequence getSequence();

}
