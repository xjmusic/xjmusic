// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

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
    } catch (ValueException e) {
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
  public void validate() throws ValueException {
    super.validate();
    Value.require(programId, "Program ID");
    Value.require(name, "Name");

    Value.requireNo(typeException, "Type");
    Value.require(type, "Type");
  }
}
