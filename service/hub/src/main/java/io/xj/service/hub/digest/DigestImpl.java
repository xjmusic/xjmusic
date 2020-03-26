// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import io.xj.lib.rest_api.PayloadObject;
import io.xj.service.hub.HubException;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.ingest.Ingest;
import io.xj.service.hub.ingest.IngestState;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal Ingest Provider, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public abstract class DigestImpl extends Entity implements Digest {
  protected final Ingest ingest;
  protected IngestState state;
  protected DigestType type;

  /**
   Instantiate a new digest with a collection of target entities
   */
  protected DigestImpl(Ingest ingest, DigestType type) {
    this.ingest = ingest;
    this.type = type;
  }

  /*

     [#166746925] DEPRECATE SUPERSEQENCE/SUPERPATTERN FOR NOW

   Retrieved a cached audio

   @param id of audio
   @return audio
   *
  public Audio getAudio(BigInteger id) {
    return ingest.getAudioMap().get(id);
  }

  /**
   Retrieved a cached sequence

   @param id of sequence
   @return sequence
   *
  public Sequence getProgram(BigInteger id) {
    return ingest.getProgramMap().get(id);
  }

  /**
   Retrieved a cached pattern

   @param id of pattern
   @return pattern
   *
  public Pattern getPattern(BigInteger id) {
    return ingest.getPatternMap().get(id);
  }

  /**
   Retrieved a cached instrument

   @param id of instrument
   @return instrument
   *
  public Instrument getInstrument(BigInteger id) {
    return ingest.getInstrumentMap().get(id);
  }

   */

  /**
   Get state of ingest

   @return state of ingest
   */
  public IngestState getState() {
    return state;
  }

  /**
   Set state of ingest

   @param state to set
   @return ingest, for method chaining
   */
  public DigestImpl setState(IngestState state) {
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
    // TODO actually return a payload object of the digest
    return new PayloadObject();
  }
}
