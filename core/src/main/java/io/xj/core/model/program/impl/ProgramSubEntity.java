//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.program.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.SubEntityImpl;
import io.xj.core.model.program.Program;

import java.math.BigInteger;

/**
 [#166132897] Program model handles all of its own entities
 [#166273140] Program Child Entities are identified and related by UUID (not id)
 */
public abstract class ProgramSubEntity extends SubEntityImpl {
  private BigInteger programId;

  @Override
  public BigInteger getParentId() {
    return programId;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Program.class)
      .build();
  }

  /**
   Get id of Program to which this entity belongs

   @return program id
   */
  public BigInteger getProgramId() {
    return programId;
  }

  /**
   Set id of Program to which this entity belongs

   @param programId to which this entity belongs
   @return this Program Entity (for chaining setters)
   */
  public ProgramSubEntity setProgramId(BigInteger programId) {
    this.programId = programId;
    return this;
  }

  @Override
  public ProgramSubEntity validate() throws CoreException {
    require(programId, "Program ID");
    return this;
  }
}
