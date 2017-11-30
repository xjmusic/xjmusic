// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.idea_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.MemeEntity;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.IDEA_MEME;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class IdeaMeme extends MemeEntity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "ideaMeme";
  public static final String KEY_MANY = "ideaMemes";
  // Idea ID
  private ULong ideaId;

  public ULong getIdeaId() {
    return ideaId;
  }

  public IdeaMeme setIdeaId(BigInteger ideaId) {
    this.ideaId = ULong.valueOf(ideaId);
    return this;
  }

  @Override
  public IdeaMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.ideaId == null) {
      throw new BusinessException("Idea ID is required.");
    }
    super.validate();
  }

  @Override
  public IdeaMeme setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(IDEA_MEME.ID);
    ideaId = record.get(IDEA_MEME.IDEA_ID);
    name = record.get(IDEA_MEME.NAME);
    createdAt = record.get(IDEA_MEME.CREATED_AT);
    updatedAt = record.get(IDEA_MEME.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(IDEA_MEME.IDEA_ID, ideaId);
    fieldValues.put(IDEA_MEME.NAME, name);
    return fieldValues;
  }

}
