// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.ingest.HubIngest;

/**
 Encapsulate generation-related functionality, by providing an HubIngest of source entities
 */
@FunctionalInterface
public interface HubGenerationFactory {

  /**
   [#154548999] Artist wants to generate a Library Supersequence in order to of a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.

   @param sequence target to build generation around
   @param ingest   HubIngest to be generated of
   @return HubGenerationLibrarySupersequence
   @throws HubGenerationException on failure
   */
  HubGenerationLibrarySupersequence librarySupersequence(
    @Assisted("sequence") ProgramSequence sequence,
    @Assisted("ingest") HubIngest ingest
  ) throws HubGenerationException;

}
