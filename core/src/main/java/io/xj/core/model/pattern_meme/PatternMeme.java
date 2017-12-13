// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.meme.Meme;
import io.xj.core.util.Text;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.PATTERN_MEME;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class PatternMeme extends Meme {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "patternMeme";
  public static final String KEY_MANY = "patternMemes";

  // Pattern ID
  private ULong patternId;

  public ULong getPatternId() {
    return patternId;
  }

  public PatternMeme setPatternId(BigInteger patternId) {
    this.patternId = ULong.valueOf(patternId);
    return this;
  }

  @Override
  public PatternMeme setName(String name) {
    this.name = Text.toProperSlug(name);
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == patternId) {
      throw new BusinessException("Pattern ID is required.");
    }
    super.validate();
  }

  @Override
  public PatternMeme setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(PATTERN_MEME.ID);
    patternId = record.get(PATTERN_MEME.PATTERN_ID);
    name = record.get(PATTERN_MEME.NAME);
    createdAt = record.get(PATTERN_MEME.CREATED_AT);
    updatedAt = record.get(PATTERN_MEME.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PATTERN_MEME.PATTERN_ID, patternId);
    fieldValues.put(PATTERN_MEME.NAME, name);
    return fieldValues;
  }

}
