//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.entity.MemeEntity;
import io.xj.core.exception.CoreException;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramSequenceBindingMeme extends MemeEntity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(MemeEntity.RESOURCE_ATTRIBUTE_NAMES)
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Program.class)
    .add(ProgramSequenceBinding.class)
    .build();
  private UUID programId;
  private UUID programSequenceBindingId;

  /**
   Create a new SequenceBindingMeme

   @param sequenceBinding to of meme in
   @param name            of meme
   @return new sequence binding meme
   */
  public static ProgramSequenceBindingMeme create(ProgramSequenceBinding sequenceBinding, String name) {
    return create()
      .setProgramId(sequenceBinding.getProgramId())
      .setSequenceBinding(sequenceBinding)
      .setName(name);
  }

  /**
   Create a new SequenceBindingMeme

   @return new sequence binding meme
   */
  public static ProgramSequenceBindingMeme create() {
    return (ProgramSequenceBindingMeme) new ProgramSequenceBindingMeme().setId(UUID.randomUUID());
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
  public ProgramSequenceBindingMeme setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get sequence binding UUID

   @return sequence binding UUID
   */
  public UUID getProgramSequenceBindingId() {
    return programSequenceBindingId;
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
  public ProgramSequenceBindingMeme setName(String name) {
    super.setName(name);
    return this;
  }

  /**
   Set Sequence Binding UUID by providing the sequence binding itself

   @param sequenceBinding to set UUID of
   @return this Sequence Binding MemeEntity (for chaining methods)
   */
  public ProgramSequenceBindingMeme setSequenceBinding(ProgramSequenceBinding sequenceBinding) {
    return setProgramSequenceBindingId(sequenceBinding.getId());
  }

  /**
   Set Sequence Binding UUID

   @param programSequenceBindingId to set
   @return this Sequence Binding MemeEntity (for chaining methods)
   */
  public ProgramSequenceBindingMeme setProgramSequenceBindingId(UUID programSequenceBindingId) {
    this.programSequenceBindingId = programSequenceBindingId;
    return this;
  }

  @Override
  public String toString() {
    return String.format("SequenceBinding[%s]-%s", getProgramSequenceBindingId(), getName());
  }

  @Override
  public void validate() throws CoreException {
    require(programId, "Program ID");
    require(programSequenceBindingId, "Sequence Binding ID");
    MemeEntity.validate(this);
  }

}
