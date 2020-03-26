// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramVoiceTrack extends Entity {


  private UUID programId;
  private UUID programVoiceId;
  private String name;

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

  @Override
  public UUID getParentId() {
    return programId;
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

  @Override
  public void validate() throws ValueException {
    super.validate();
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
