// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.CHAIN_IDEA;

public class ChainIdea extends Entity {

  // Chain ID
  private BigInteger chainId;

  public ULong getChainId() {
    return ULong.valueOf(chainId);
  }

  public ChainIdea setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  // Idea ID
  private BigInteger ideaId;

  public ULong getIdeaId() {
    return ULong.valueOf(ideaId);
  }

  public ChainIdea setIdeaId(BigInteger ideaId) {
    this.ideaId = ideaId;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.chainId == null) {
      throw new BusinessException("Chain ID is required.");
    }
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
  }

  /**
   * Model info jOOQ-field : Value map
   *
   * @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHAIN_IDEA.CHAIN_ID, chainId);
    fieldValues.put(CHAIN_IDEA.IDEA_ID, ideaId);
    return fieldValues;
  }

  /**
   * For use in maps.
   */
  public static final String KEY_ONE = "chainIdea";
  public static final String KEY_MANY = "chainIdeas";


}
