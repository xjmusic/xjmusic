// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.generation;

import io.xj.core.evaluation.Evaluation;
import io.xj.core.exception.ConfigException;
import io.xj.core.generation.superpattern.LibrarySuperpatternGeneration;
import io.xj.core.model.pattern.Pattern;

import com.google.inject.assistedinject.Assisted;

/**
 Encapsulate generation-related functionality, by providing an Evaluation of source entities
 */
@FunctionalInterface
public interface GenerationFactory {

  /**
   [#154548999] Artist wants to generate a Library Superpattern in order to create a Detail pattern that covers the chord progressions of all existing Main Patterns in a Library.

   @param pattern    target to build generation around
   @param evaluation Evaluation to be generated from
   @return LibrarySuperpatternGeneration
   @throws ConfigException on failure
   */
  LibrarySuperpatternGeneration librarySuperpattern(
    @Assisted("pattern") Pattern pattern,
    @Assisted("evaluation") Evaluation evaluation
  ) throws ConfigException;

}
