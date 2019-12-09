// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

import java.util.UUID;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramVoice extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("type")
    .add("name")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Program.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(ProgramSequencePattern.class)
    .build();
  private UUID programId;
  private InstrumentType type;
  private String name;
  private Exception typeException;

  /**
   Create a new voice

   @param type of voice
   @param name of voice
   @return new Voice
   */
  public static ProgramVoice create(Program program, InstrumentType type, String name) {
    return create(program)
      .setTypeEnum(type)
      .setName(name);
  }

  /**
   Create a new voice

   @param type of voice
   @param name of voice
   @return new Voice
   */
  public static ProgramVoice create(Program program, String type, String name) {
    return create(program)
      .setType(type)
      .setName(name);
  }

  /**
   Create a new voice

   @return new Voice
   */
  public static ProgramVoice create(Program program) {
    return create()
      .setProgramId(program.getId());
  }

  /**
   Create a new voice

   @return new Voice
   */
  public static ProgramVoice create() {
    return new ProgramVoice().setId(UUID.randomUUID());
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
  public ProgramVoice setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get name

   @return name
   */
  public String getName() {
    return name;
  }

  @Override
  public ProgramVoice setId(UUID id) {
    super.setId(id);
    return this;
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
   Get type

   @return type
   */
  public InstrumentType getType() {
    return type;
  }

  /**
   Set name

   @param name to set
   @return this Voice (for chaining methods)
   */
  public ProgramVoice setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set type by string value

   @param type to set
   @return this Voice (for chaining methods)
   */
  public ProgramVoice setType(String type) {
    try {
      this.type = InstrumentType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set type

   @param type to set
   @return this Voice (for chaining methods)
   */
  public ProgramVoice setTypeEnum(InstrumentType type) {
    this.type = type;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();
    require(programId, "Program ID");
    require(name, "Name");

    requireNo(typeException, "Type");
    require(type, "Type");
  }
}
