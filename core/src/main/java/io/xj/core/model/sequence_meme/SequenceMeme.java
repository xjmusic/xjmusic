// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_meme;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.meme.Meme;
import io.xj.core.util.Text;

import java.math.BigInteger;
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
public class SequenceMeme extends EntityImpl implements Meme {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "sequenceMeme";
  public static final String KEY_MANY = "sequenceMemes";

  // Sequence ID
  private BigInteger sequenceId;
  private String name;

  public BigInteger getSequenceId() {
    return sequenceId;
  }

  public SequenceMeme setSequenceId(BigInteger sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  @Override
  public SequenceMeme setId(BigInteger id) {
    this.id = id;
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public SequenceMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return sequenceId;
  }

  @Override
  public void validate() throws CoreException {
    if (null == sequenceId) {
      throw new CoreException("Sequence ID is required.");
    }
    if (Objects.isNull(name) || name.isEmpty()) {
      throw new CoreException("Name is required.");
    }
  }

}
