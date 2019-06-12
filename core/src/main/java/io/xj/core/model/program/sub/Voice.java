// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.impl.ProgramSubEntity;

import java.math.BigInteger;

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
public class Voice extends ProgramSubEntity {
  private InstrumentType type;
  private String description;
  private Exception typeException;

  /**
   Get description

   @return description
   */
  public String getDescription() {
    return description;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("type")
      .add("description")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Instrument.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Pattern.class)
      .build();
  }

  /**
   Get type

   @return type
   */
  public InstrumentType getType() {
    return type;
  }

  /**
   Set description

   @param description to set
   @return this Voice (for chaining methods)
   */
  public Voice setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   Set type by string value

   @param type to set
   @return this Voice (for chaining methods)
   */
  public Voice setType(String type) {
    try {
      this.type = InstrumentType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set type

   @param type to set
   @return this Voice (for chaining methods)
   */
  public Voice setTypeEnum(InstrumentType type) {
    this.type = type;
    return this;
  }


  @Override
  public Voice setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public Voice validate() throws CoreException {
    super.validate();
    require(description, "Description");

    requireNo(typeException, "Type");
    require(type, "Type");

    return this;
  }
}
