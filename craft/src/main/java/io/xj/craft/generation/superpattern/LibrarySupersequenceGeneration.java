// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation.supersequence;

import io.xj.craft.chord.ChordNode;
import io.xj.craft.generation.Generation;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 [#154548999] Artist wants to generate a Library Supersequence in order to create a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.
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
  List<Pattern> getGeneratedPatterns();

  /**
   Get generated pattern chords.

   @return map of pattern id to list of generated pattern chords
   */
  Map<BigInteger, List<PatternChord>> getGeneratedPatternChords();

  /**
   Get the sequence that was provided as a target to generate supersequence around

   @return sequence
   */
  Sequence getSequence();

}
