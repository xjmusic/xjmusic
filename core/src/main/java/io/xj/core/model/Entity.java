// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.BusinessException;
import io.xj.core.timestamp.TimestampUTC;

import java.math.BigInteger;
import java.sql.Timestamp;

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
public abstract class Entity {

  /**
   For use in maps.
   Should be overridden by extending classes.
   */
  public static final String KEY_ID = "id";
  public static final String KEY_ONE = "entity";
  public static final String KEY_MANY = "entities";

  // fields
  protected BigInteger id;
  protected Timestamp createdAt;
  protected Timestamp updatedAt;

  public BigInteger getId() {
    return id;
  }

  public Entity setId(BigInteger id) {
    this.id = id;
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

  public void setCreatedAtTimestamp(Timestamp createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAtTimestamp(Timestamp updatedAt) {
    this.updatedAt = updatedAt;
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


}
