// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;

import javax.annotation.Nullable;
import java.util.Map;

/**
 This represents common properties of all memes,
 although a Meme only actually exists as a Link Meme, Pattern Meme, etc.
 */
public class Meme extends Entity {
  public static final String KEY_ONE = "meme";
  public static final String KEY_MANY = "memes";

  public String getName() {
    return name;
  }

  protected String name;

  public Meme setName(String name) {
    this.name = name;
    return this;
  }

  public void validate() throws BusinessException {
    if (null == name) {
      throw new BusinessException("Name is required.");
    }
  }

  /**
   Set values from record

   @param record to set values from
   @return Entity
   */
  @Nullable
  @Override
  public Entity setFromRecord(Record record) throws BusinessException {
    throw new BusinessException("There is no generic meme implementation persisted in the database!");
  }

  /**
   Model info jOOQ-field : Value map
   ONLY FOR FIELDS THAT ARE TO BE UPDATED

   @return map
   */
  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    return null;
  }

}
