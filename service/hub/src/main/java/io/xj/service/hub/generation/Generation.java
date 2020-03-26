// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import io.xj.service.hub.ingest.Ingest;

public interface Generation {

  /**
   Determine type of generation, e.g. initial segment in chain, or next macro-sequence

   @return macro-craft type
   */
  GenerationType type();

  /**
   An Ingest collection of entities that this chain segment fabrication generation will ingest.
   Based on primary chain-bindings, e.g. ChainLibrary, ChainInstrument, and ChainSequence.

   @return Ingest
   */
  Ingest ingest();

}
