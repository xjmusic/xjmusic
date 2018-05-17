// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.meme.Meme;
import io.xj.core.util.Text;

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
public class SequenceMeme extends Meme {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "sequenceMeme";
  public static final String KEY_MANY = "sequenceMemes";

  // Sequence ID
  private BigInteger sequenceId;

  public BigInteger getSequenceId() {
    return sequenceId;
  }

  public SequenceMeme setSequenceId(BigInteger sequenceId) {
    this.sequenceId = sequenceId;
    return this;
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
  public void validate() throws BusinessException {
    if (null == sequenceId) {
      throw new BusinessException("Sequence ID is required.");
    }
    super.validate();
  }

}
