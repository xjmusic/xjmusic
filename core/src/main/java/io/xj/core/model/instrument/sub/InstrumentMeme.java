// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.MemeEntity;
import io.xj.core.model.instrument.impl.InstrumentSubEntity;
import io.xj.core.util.Text;

import java.util.Objects;

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
public class InstrumentMeme extends InstrumentSubEntity implements MemeEntity {
  private String name;

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
  public InstrumentMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public InstrumentMeme validate() throws CoreException {
    super.validate();

    if (Objects.isNull(name) || name.isEmpty())
      throw new CoreException("Name is required.");

    return this;
  }

}
