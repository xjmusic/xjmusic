//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.MemeEntity;
import io.xj.core.model.program.impl.ProgramSubEntity;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class SequenceBindingMeme extends ProgramSubEntity implements MemeEntity {
  private UUID sequenceBindingId;
  private String name;

  /**
   Get sequence binding UUID

   @return sequence binding UUID
   */
  public UUID getSequenceBindingId() {
    return sequenceBindingId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(SequenceBinding.class)
      .build();
  }

  @Override
  public SequenceBindingMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public SequenceBindingMeme setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  /**
   Set Sequence Binding UUID by providing the sequence binding itself

   @param sequenceBinding to set UUID of
   @return this Sequence Binding MemeEntity (for chaining methods)
   */
  public SequenceBindingMeme setSequenceBinding(SequenceBinding sequenceBinding) {
    return setSequenceBindingId(sequenceBinding.getId());
  }

  /**
   Set Sequence Binding UUID

   @param sequenceBindingId to set
   @return this Sequence Binding MemeEntity (for chaining methods)
   */
  public SequenceBindingMeme setSequenceBindingId(UUID sequenceBindingId) {
    this.sequenceBindingId = sequenceBindingId;
    return this;
  }

  @Override
  public String toString() {
    return String.format("SequenceBinding[%s]-%s", getSequenceBindingId(), name);
  }

  @Override
  public SequenceBindingMeme validate() throws CoreException {
    super.validate();
    require(sequenceBindingId, "Sequence Binding ID");
    require(name, "Name");
    return this;
  }

}
