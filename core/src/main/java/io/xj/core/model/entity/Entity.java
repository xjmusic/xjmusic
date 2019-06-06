//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import io.xj.core.exception.CoreException;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.time.Instant;

public interface Entity {
  String KEY_ONE = "entity";
  String KEY_MANY = "entities";

  /**
   Get entity id

   @return entity id
   */
  BigInteger getId();

  /**
   Set entity id

   @param id to set
   @return entity
   */
  Entity setId(BigInteger id);

  /**
   Get parent id

   @return parent id
   */
  BigInteger getParentId();

  /**
   Get created-at instant

   @return created-at instant
   */
  @Nullable
  Instant getCreatedAt();

  /**
   Set created at time

   @param createdAt time
   @return entity
   */
  Entity setCreatedAt(String createdAt);

  /**
   Get updated-at time

   @return updated-at time
   */
  @Nullable
  Instant getUpdatedAt();

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  Entity setUpdatedAt(String updatedAt);

  /**
   Validate data.

   @throws CoreException if invalid.
   */
  void validate() throws CoreException;
}
