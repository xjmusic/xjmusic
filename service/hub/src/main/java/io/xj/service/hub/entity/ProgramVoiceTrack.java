// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.Objects;
import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramVoiceTrack extends Entity {
  private static final Float DEFAULT_ORDER_VALUE = 1000.0f;
  private UUID programId;
  private UUID programVoiceId;
  private String name;
  private Float order;

  /**
   Create a new Track

   @return new track
   */
  public static ProgramVoiceTrack create() {
    return (ProgramVoiceTrack) new ProgramVoiceTrack().setId(UUID.randomUUID());
  }

  /**
   Create a new Track

   @param voice of track
   @param name  of track
   @return new track
   */
  public static ProgramVoiceTrack create(ProgramVoice voice, String name) {
    return create()
      .setProgramId(voice.getProgramId())
      .setVoice(voice)
      .setName(name);
  }

  /**
   Get id of Program to which this entity belongs

   @return program id
   */
  public UUID getProgramId() {
    return programId;
  }

  /**
   Set id of Program to which this entity belongs

   @param programId to which this entity belongs
   @return this Program Entity (for chaining setters)
   */
  public ProgramVoiceTrack setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  /**
   Get Voice ID

   @return voice id
   */
  public UUID getProgramVoiceId() {
    return programVoiceId;
  }

  /**
   Set Name

   @param name to set
   @return this Track (for chaining setters)
   */
  public ProgramVoiceTrack setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set voice UUID by providing a Voice entity

   @param voice id to set
   @return this Track (for chaining setters)
   */
  public ProgramVoiceTrack setVoice(ProgramVoice voice) {
    setProgramVoiceId(voice.getId());
    return this;
  }

  /**
   Set Voice ID

   @param programVoiceId to set
   @return this Track (for chaining setters)
   */
  public ProgramVoiceTrack setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
    return this;
  }

  /**
   get the order

   @return order
   */
  public Float getOrder() {
    return order;
  }

  /**
   set the order

   @param order to set
   @return this ProgramVoiceTrack (for chaining methods)
   */
  public ProgramVoiceTrack setOrder(Float order) {
    this.order = order;
    return this;
  }


  @Override
  public void validate() throws ValueException {
    super.validate();
    if (Objects.isNull(order)) order = DEFAULT_ORDER_VALUE;

    Value.require(programId, "Program ID");
    Value.require(programVoiceId, "Voice ID");
    Value.require(name, "Name");
    name = Text.toUpperSlug(name);
  }

  /**
   @return String representation of Track
   */
  public String toString() {
    return name;
  }
}
