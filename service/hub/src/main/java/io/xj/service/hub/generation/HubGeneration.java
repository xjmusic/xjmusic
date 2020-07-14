// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import io.xj.service.hub.ingest.HubIngest;

public interface HubGeneration {

  /**
   Determine type of generation, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  HubGenerationType type();

  /**
   An HubIngest collection of entities that this chain segment fabrication generation will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainSequence.

   @return HubIngest
   */
  HubIngest ingest();

}
