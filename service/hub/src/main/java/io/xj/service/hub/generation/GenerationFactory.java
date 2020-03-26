// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.HubException;
import io.xj.service.hub.ingest.Ingest;
import io.xj.service.hub.model.ProgramSequence;

/**
 Encapsulate generation-related functionality, by providing an Ingest of source entities
 */
@FunctionalInterface
public interface GenerationFactory {

  /**
   [#154548999] Artist wants to generate a Library Supersequence in order to of a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.

   @param sequence    target to build generation around
   @param ingest Ingest to be generated of
   @return LibrarySupersequenceGeneration
   @throws HubException on failure
   */
  LibrarySupersequenceGeneration librarySupersequence(
    @Assisted("sequence") ProgramSequence sequence,
    @Assisted("ingest") Ingest ingest
  ) throws HubException;

}
