// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import io.xj.service.hub.ingest.HubIngest;
import io.xj.service.hub.ingest.HubIngestState;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal HubIngest Provider, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public abstract class HubGenerationImpl implements HubGeneration {
  protected final HubIngest ingest;
  protected HubIngestState state;
  protected HubGenerationType type;

  /**
   Instantiate a new generation with a collection of target entities
   */
  protected HubGenerationImpl(HubIngest ingest, HubGenerationType type) {
    this.ingest = ingest;
    this.type = type;
  }

  /**
   Get state of ingest

   @return state of ingest
   */
  public HubIngestState getState() {
    return state;
  }

  /**
   Set state of ingest

   @param state to set
   @return ingest, for method chaining
   */
  public HubGenerationImpl setState(HubIngestState state) {
    this.state = state;
    return this;
  }

  /**
   Get type of ingest

   @return type
   */
  public HubGenerationType getType() {
    return type;
  }


}
