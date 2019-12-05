// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramSequenceBinding extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("offset")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Program.class)
    .add(ProgramSequence.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(ProgramSequenceBindingMeme.class)
    .build();
  private UUID programId;
  private UUID programSequenceId;
  private Long offset;

  /**
   Create a new SequenceBinding

   @param sequence to of binding in
   @param offset   of binding
   @return new sequence binding
   */
  public static ProgramSequenceBinding create(ProgramSequence sequence, long offset) {
    return create()
      .setProgramId(sequence.getProgramId())
      .setSequence(sequence)
      .setOffset(offset);
  }

  /**
   Create a new SequenceBinding

   @return new sequence binding
   */
  public static ProgramSequenceBinding create() {
    return (ProgramSequenceBinding) new ProgramSequenceBinding().setId(UUID.randomUUID());
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
  public ProgramSequenceBinding setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get offset

   @return offset
   */
  public Long getOffset() {
    return offset;
  }

  /**
   Get Sequence id

   @return sequence id
   */
  public UUID getProgramSequenceId() {
    return programSequenceId;
  }

  /**
   Set offset

   @param offset to set
   @return this sequence program
   */
  public ProgramSequenceBinding setOffset(Long offset) {
    this.offset = offset;
    return this;
  }

  /**
   Set sequence UUID by providing a Sequence entity

   @param sequence id to set
   @return this SequenceBinding (for chaining setters)
   */
  public ProgramSequenceBinding setSequence(ProgramSequence sequence) {
    setProgramSequenceId(sequence.getId());
    return this;
  }

  /**
   Set sequence id

   @param programSequenceId to set
   @return sequence program
   */
  public ProgramSequenceBinding setProgramSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
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

  @Override
  public String toString() {
    return String.format("Program[%s]-Sequence[%s]@%d", getProgramId(), programSequenceId, offset);
  }

  @Override
  public void validate() throws CoreException {
    super.validate();
    require(programId, "Program ID");
    require(programSequenceId, "Sequence ID");
    require(offset, "Offset");
  }

}
