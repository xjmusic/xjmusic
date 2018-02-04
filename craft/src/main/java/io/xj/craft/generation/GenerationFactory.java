// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation;

import io.xj.craft.ingest.Ingest;
import io.xj.core.exception.ConfigException;
import io.xj.craft.generation.superpattern.LibrarySuperpatternGeneration;
import io.xj.core.model.pattern.Pattern;

import com.google.inject.assistedinject.Assisted;

/**
 Encapsulate generation-related functionality, by providing an Ingest of source entities
 */
@FunctionalInterface
public interface GenerationFactory {

  /**
   [#154548999] Artist wants to generate a Library Superpattern in order to create a Detail pattern that covers the chord progressions of all existing Main Patterns in a Library.

   @param pattern    target to build generation around
   @param ingest Ingest to be generated from
   @return LibrarySuperpatternGeneration
   @throws ConfigException on failure
   */
  LibrarySuperpatternGeneration librarySuperpattern(
    @Assisted("pattern") Pattern pattern,
    @Assisted("ingest") Ingest ingest
  ) throws ConfigException;

}
