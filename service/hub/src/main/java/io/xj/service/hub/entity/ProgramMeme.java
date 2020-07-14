// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.MemeEntity;
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
public class ProgramMeme extends MemeEntity {


  private UUID programId;

  /**
   Create a new program meme

   @param name of meme
   @return meme
   */
  public static ProgramMeme create(Program program, String name) {
    return create()
      .setProgramId(program.getId())
      .setName(name);
  }

  /**
   of Program MemeEntity

   @return new Program MemeEntity
   */
  public static ProgramMeme create() {
    return new ProgramMeme().setId(UUID.randomUUID());
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
  public ProgramMeme setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  @Override
  public ProgramMeme setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public ProgramMeme setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public String toString() {
    return String.format("Program[%s]-%s", getProgramId(), getName());
  }

  @Override
  public void validate() throws ValueException {
    Value.require(programId, "Program ID");
    MemeEntity.validate(this);
  }

}
