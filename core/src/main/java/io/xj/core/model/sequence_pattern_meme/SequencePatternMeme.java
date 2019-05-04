// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern_meme;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
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
public class SequencePatternMeme extends EntityImpl implements Meme {
  public static final String KEY_ONE = "sequencePatternMeme";
  public static final String KEY_MANY = "sequencePatternMemes";
  private BigInteger sequencePatternId;
  private String name;

  public BigInteger getSequencePatternId() {
    return sequencePatternId;
  }

  public SequencePatternMeme setSequencePatternId(BigInteger sequencePatternId) {
    this.sequencePatternId = sequencePatternId;
    return this;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public SequencePatternMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return sequencePatternId;
  }

  @Override
  public void validate() throws CoreException {
    if (Objects.isNull(sequencePatternId)) {
      throw new CoreException("Sequence Pattern ID is required.");
    }
    if (Objects.isNull(name) || name.isEmpty()) {
      throw new CoreException("Name is required.");
    }
  }

}
