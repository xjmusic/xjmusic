// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.outright.xj.core.Tables.CHAIN_IDEA;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ChainIdea extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "chainIdea";
  public static final String KEY_MANY = "chainIdeas";
  // Chain ID
  private ULong chainId;
  // Idea ID
  private ULong ideaId;

  public ULong getChainId() {
    return chainId;
  }

  public ChainIdea setChainId(BigInteger chainId) {
    this.chainId = ULong.valueOf(chainId);
    return this;
  }

  public ULong getIdeaId() {
    return ideaId;
  }

  public ChainIdea setIdeaId(BigInteger ideaId) {
    this.ideaId = ULong.valueOf(ideaId);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
  }

  @Override
  public ChainIdea setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(CHAIN_IDEA.ID);
    chainId = record.get(CHAIN_IDEA.CHAIN_ID);
    ideaId = record.get(CHAIN_IDEA.IDEA_ID);
    createdAt = record.get(CHAIN_IDEA.CREATED_AT);
    updatedAt = record.get(CHAIN_IDEA.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_IDEA.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_IDEA.IDEA_ID, ideaId);
    return fieldValues;
  }


}
