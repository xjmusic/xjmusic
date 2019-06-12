// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.segment.impl.SegmentSubEntity;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class Choice extends SegmentSubEntity {
  private BigInteger programId;
  private ProgramType type;
  private Integer transpose;
  private UUID sequenceBindingId;

  /**
   Get transpose

   @return transpose
   */
  public Integer getTranspose() {
    return transpose;
  }

  @Override
  public BigInteger getParentId() {
    return getSegmentId();
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("type")
      .add("transpose")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Program.class)
      .add(SequenceBinding.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Arrangement.class)
      .build();
  }

  /**
   Get program id

   @return program id
   */
  public BigInteger getProgramId() {
    return programId;
  }

  /**
   Sequence Binding UUID

   @return sequence binding id
   */
  public UUID getSequenceBindingId() {
    return sequenceBindingId;
  }

  /**
   Get type

   @return type
   */
  public ProgramType getType() {
    return type;
  }

  /**
   Set program ID

   @param programId to set
   @return this Choice (for chaining setters)
   */
  public Choice setProgramId(BigInteger programId) {
    this.programId = programId;
    return this;
  }

  @Override
  public Choice setSegmentId(BigInteger segmentId) {
    super.setSegmentId(segmentId);
    return this;
  }

  /**
   Set Sequence Binding UUID by providing a sequence binding

   @param sequenceBinding to set id of
   @return this Choice (for chaining setters)
   */
  public Choice setSequenceBinding(SequenceBinding sequenceBinding) {
    setSequenceBindingId(sequenceBinding.getId());
    return this;
  }

  /**
   Set Sequence Binding UUID

   @param sequenceBindingId to set
   @return this Choice (for chaining setters)
   */
  public Choice setSequenceBindingId(UUID sequenceBindingId) {
    this.sequenceBindingId = sequenceBindingId;
    return this;
  }

  /**
   Set type

   @param type to set
   @return this Choice (for chaining setters)
   */
  public Choice setType(String type) {
    this.type = ProgramType.valueOf(type);
    return this;
  }

  /**
   Set type

   @param type to set
   @return this Choice (for chaining setters)
   */
  public Choice setTypeEnum(ProgramType type) {
    this.type = type;
    return this;
  }

  /**
   Set transpose +/- semitone

   @param transpose to set
   @return this Choice (for chaining setters)
   */
  public Choice setTranspose(Integer transpose) {
    this.transpose = transpose;
    return this;
  }

  @Override
  public Choice setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public Choice validate() throws CoreException {
    super.validate();
    require(programId, "Program ID");
    require(type, "Type");
    if (isEmpty(transpose)) transpose = 0;
    return this;
  }
}
