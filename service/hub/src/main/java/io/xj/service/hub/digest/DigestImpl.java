// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.PayloadObject;
import io.xj.service.hub.HubException;
import io.xj.service.hub.ingest.HubIngest;
import io.xj.service.hub.ingest.HubIngestState;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal HubIngest Provider, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public abstract class DigestImpl extends Entity implements Digest {
  protected final HubIngest ingest;
  protected HubIngestState state;
  protected DigestType type;

  /**
   Instantiate a new digest with a collection of target entities
   */
  protected DigestImpl(HubIngest ingest, DigestType type) {
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
  public DigestImpl setState(HubIngestState state) {
    this.state = state;
    return this;
  }

  /**
   Get type of ingest

   @return type
   */
  public DigestType getType() {
    return type;
  }

  /**
   Create a new HubException prefixed with a segment id

   @param message to include in exception
   @return HubException to throw
   */
  public HubException exception(String message) {
    return new HubException(formatLog(message));
  }

  /**
   Create a new HubException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return HubException to throw
   */
  public HubException exception(String message, Exception e) {
    return new HubException(formatLog(message), e);
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private String formatLog(String message) {
    return String.format("[digest|type=%s|state=%s] %s", type, state, message);
  }


  @Override
  public PayloadObject getPayloadObject() {
    // FUTURE actually return a payload object of the digest
    return new PayloadObject();
  }
}
