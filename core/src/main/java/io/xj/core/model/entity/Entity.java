// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity;

import io.xj.core.exception.BusinessException;
import io.xj.core.util.TimestampUTC;

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
  public static final String KEY_ONE = "entity";
  public static final String KEY_MANY = "entities";
  private static final double entityPositionDecimalPlaces = 2;
  private static final double roundPositionMultiplier = Math.pow(10, entityPositionDecimalPlaces);
  protected BigInteger id;
  protected Timestamp createdAt;
  protected Timestamp updatedAt;

  /**
   Get entity id

   @return entity id
   */
  public BigInteger getId() {
    return id;
  }

  /**
   Set entity id

   @param id to set
   @return entity
   */
  public Entity setId(BigInteger id) {
    this.id = id;
    return this;
  }

  /**
   Get parent id

   @return parent id
   */
  public abstract BigInteger getParentId();

  /**
   Get created-at timestamp

   @return created-at timestamp
   */
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = TimestampUTC.valueOf(createdAt);
    } catch (Exception e) {
      this.createdAt = null;
    }
    return this;
  }

  /**
   Get updated-at time

   @return updated-at time
   */
  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = TimestampUTC.valueOf(updatedAt);
    } catch (Exception e) {
      this.updatedAt = null;
    }
    return this;
  }

  /**
   Round a position to N decimal places.
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.

   @param position to round
   @return rounded position
   */
  protected static Double roundPosition(Double position) {
    return Math.floor(position * roundPositionMultiplier) / roundPositionMultiplier;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public abstract void validate() throws BusinessException;
}
