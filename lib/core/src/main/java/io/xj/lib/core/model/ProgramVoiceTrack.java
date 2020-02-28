// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.util.Text;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramVoiceTrack extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("name")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Program.class)
    .add(ProgramVoice.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(ProgramSequencePatternEvent.class)
    .build();
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

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return RESOURCE_HAS_MANY;
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
  public void validate() throws CoreException {
    super.validate();
    require(programId, "Program ID");
    require(programVoiceId, "Voice ID");
    require(name, "Name");
    name = Text.toUpperSlug(name);
  }

  /**
   @return String representation of Track
   */
  public String toString() {
    return name;
  }
}
