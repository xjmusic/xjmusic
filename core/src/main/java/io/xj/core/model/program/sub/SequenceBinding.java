//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.impl.ProgramSubEntity;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class SequenceBinding extends ProgramSubEntity {
  private UUID sequenceId;
  private Long offset;

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
  public UUID getSequenceId() {
    return sequenceId;
  }

  /**
   Set offset

   @param offset to set
   @return this sequence program
   */
  public SequenceBinding setOffset(Long offset) {
    this.offset = offset;
    return this;
  }

  /**
   Set sequence UUID by providing a Sequence entity

   @param sequence id to set
   @return this SequenceBinding (for chaining setters)
   */
  public SequenceBinding setSequence(Sequence sequence) {
    setSequenceId(sequence.getId());
    return this;
  }

  /**
   Set sequence id

   @param sequenceId to set
   @return sequence program
   */
  public SequenceBinding setSequenceId(UUID sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("offset")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Sequence.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(SequenceBindingMeme.class)
      .build();
  }

  @Override
  public SequenceBinding setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public String toString() {
    return String.format("Program[%d]-Sequence[%s]@%d", getProgramId(), sequenceId, offset);
  }

  @Override
  public SequenceBinding validate() throws CoreException {
    super.validate();
    require(sequenceId, "Sequence ID");
    require(offset, "Offset");
    return this;
  }

}
