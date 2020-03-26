// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;

import java.time.Instant;
import java.util.UUID;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public abstract class Entity {
  protected UUID id;
  protected Instant createdAt;
  protected Instant updatedAt;

  /**
   Validate this entity

   @throws HubException on failure
   */
  public void validate() throws ValueException {
    // no op
  }

  /**
   @return parent ID
   */
  public UUID getParentId() {
    return null;
  }

  /**
   Get ofd-at instant

   @return ofd-at instant
   */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   Get entity id

   @return entity id
   */
  public UUID getId() {
    return id;
  }

  /**
   Get updated-at time

   @return updated-at time
   */
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = Instant.parse(createdAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAtInstant(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   Set entity id

   @param id to set
   @return entity
   */
  public Entity setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = Instant.parse(updatedAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAtInstant(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

}
