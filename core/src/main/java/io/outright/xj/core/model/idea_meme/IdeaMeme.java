// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.Purify;

import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;

import static io.outright.xj.core.Tables.IDEA_MEME;

public class IdeaMeme extends Entity {

  // Idea ID
  private BigInteger ideaId;

  public ULong getIdeaId() {
    return ULong.valueOf(ideaId);
  }

  public IdeaMeme setIdeaId(BigInteger ideaId) {
    this.ideaId = ideaId;
    return this;
  }

  // Name
  private String name;

  public String getName() {
    return name;
  }

  public IdeaMeme setName(String name) {
    this.name = Purify.ProperSlug(name);
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
    if (this.name == null) {
      throw new BusinessException("Name is required.");
    }
  }

  /**
   Model info jOOQ-field : Value map

   @return map
   */
  public Map<Field, Object> intoFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(IDEA_MEME.IDEA_ID, ideaId);
    fieldValues.put(IDEA_MEME.NAME, name);
    return fieldValues;
  }

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "ideaMeme";
  public static final String KEY_MANY = "ideaMemes";

}
