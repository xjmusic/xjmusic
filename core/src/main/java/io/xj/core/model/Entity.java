// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.BusinessException;
import io.xj.core.timestamp.TimestampUTC;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public abstract class Entity {

  /**
   For use in maps.
   Should be overridden by extending classes.
   */
  public static final String KEY_ID = "id";
  public static final String KEY_ONE = "entity";
  public static final String KEY_MANY = "entities";

  // fields
  protected ULong id;
  protected Timestamp createdAt;
  protected Timestamp updatedAt;

  public ULong getId() {
    return id;
  }

  public Entity setId(BigInteger id) {
    this.id = ULong.valueOf(id);
    return this;
  }

  public Timestamp getCreatedAt() {
    return createdAt;
  }

  public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = TimestampUTC.valueOf(createdAt);
    } catch (Exception e) {
      this.createdAt = null;
    }
    return this;
  }

  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = TimestampUTC.valueOf(updatedAt);
    } catch (Exception e) {
      this.updatedAt = null;
    }
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public abstract void validate() throws BusinessException;


  /**
   Set values from record

   @param record to set values from
   @return Entity
   */
  @Nullable
  public abstract Entity setFromRecord(Record record) throws BusinessException;

  /**
   Model info jOOQ-field : Value map
   ONLY FOR FIELDS THAT ARE TO BE UPDATED

   @return map
   */
  public abstract Map<Field, Object> updatableFieldValueMap();

}
