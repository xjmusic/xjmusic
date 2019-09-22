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
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramMeme extends ProgramSubEntity implements MemeEntity {
  private String name;

  @Override
  public ProgramMeme setId(UUID id) {
    this.id = id;
    return this;
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
  public ProgramMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public ProgramMeme setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public String toString() {
    return String.format("Program[%s]-%s", getProgramId(), name);
  }

  @Override
  public ProgramMeme validate() throws CoreException {
    super.validate();
    require(name, "Name");
    return this;
  }

}
