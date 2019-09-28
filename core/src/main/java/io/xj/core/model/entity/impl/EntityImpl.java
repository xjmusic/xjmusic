//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.payload.PayloadObject;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Objects;

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
public abstract class EntityImpl extends ResourceEntityImpl implements Entity {
  private static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.of("createdAt", "updatedAt");
  protected BigInteger id;
  protected Instant createdAt;
  protected Instant updatedAt;

  @Override
  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public BigInteger getId() {
    return id;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public String getResourceId() {
    return String.valueOf(id);
  }

  @Override
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public Entity consume(PayloadObject payloadObject) throws CoreException {
    super.consume(payloadObject);

    if (Objects.nonNull(payloadObject.getId()) && Value.isInteger(payloadObject.getId()))
      setId(new BigInteger(payloadObject.getId()));

    return this;
  }

  @Override
  public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = Instant.parse(createdAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  @Override
  public Entity setCreatedAtInstant(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  @Override
  public Entity setId(BigInteger id) {
    this.id = id;
    return this;
  }

  @Override
  public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = Instant.parse(updatedAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  @Override
  public Entity setUpdatedAtInstant(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

}
