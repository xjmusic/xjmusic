// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

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
public class ProgramSequenceBinding extends Entity {
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
  public String toString() {
    return String.format("Program[%s]-Sequence[%s]@%d", getProgramId(), programSequenceId, offset);
  }

  @Override
  public void validate() throws ValueException {
    super.validate();
    Value.require(programId, "Program ID");
    Value.require(programSequenceId, "Sequence ID");
    Value.require(offset, "Offset");
  }

}
