//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.entity.MemeEntity;

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
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(MemeEntity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Program.class)
    .build();
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
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
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
  public void validate() throws CoreException {
    require(programId, "Program ID");
    MemeEntity.validate(this);
  }

}
