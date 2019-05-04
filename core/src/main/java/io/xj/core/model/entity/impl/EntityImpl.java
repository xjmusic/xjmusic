//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity.impl;

import io.xj.core.model.entity.Entity;
import io.xj.core.util.TimestampUTC;

import javax.annotation.Nullable;
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
public abstract class EntityImpl implements Entity {
  protected BigInteger id;
  @Nullable
  protected Timestamp createdAt;
  @Nullable
  protected Timestamp updatedAt;

  @Override public BigInteger getId() {
    return id;
  }

  @Override public Entity setId(BigInteger id) {
    this.id = id;
    return this;
  }

  @Override@Nullable
  public Timestamp getCreatedAt() {
    return createdAt;
  }

  @Override public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = TimestampUTC.valueOf(createdAt);
    } catch (Exception e) {
      this.createdAt = null;
    }
    return this;
  }

  @Override@Nullable
  public Timestamp getUpdatedAt() {
    return updatedAt;
  }

  @Override public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = TimestampUTC.valueOf(updatedAt);
    } catch (Exception e) {
      this.updatedAt = null;
    }
    return this;
  }

  }
