// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.generation.superpattern;

import io.xj.core.model.chord.ChordNode;
import io.xj.craft.generation.Generation;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 [#154548999] Artist wants to generate a Library Superpattern in order to create a Detail pattern that covers the chord progressions of all existing Main Patterns in a Library.
 */
public interface LibrarySuperpatternGeneration extends Generation {

  /**
   Get a collection of the unique chord nodes covered by this generated library super pattern.

   @return collection of chord nodes
   */
  Collection<ChordNode> getCoveredNodes();

  /**
   Get generated phases

   @return generated phases
   */
  List<Phase> getGeneratedPhases();

  /**
   Get generated phase chords.

   @return map of phase id to list of generated phase chords
   */
  Map<BigInteger, List<PhaseChord>> getGeneratedPhaseChords();

  /**
   Get the pattern that was provided as a target to generate superpattern around

   @return pattern
   */
  Pattern getPattern();

}
