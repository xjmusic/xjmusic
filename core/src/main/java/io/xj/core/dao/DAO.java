// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.entity.Entity;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface DAO<N extends Entity> {

  /**
   Create a new Record

   @param access control
   @param entity for the new Record
   @return newly readMany record
   */
  N create(Access access, N entity) throws Exception;

  /**
   Fetch one record  if accessible

   @param access control
   @param id     of record to fetch
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  N readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch many records for many parents by id, if accessible

   @param access    control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws Exception on failure
   */
  Collection<N> readAll(Access access, Collection<BigInteger> parentIds) throws Exception;


  /**
   Update a specified Entity

   @param access control
   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  void update(Access access, BigInteger id, N entity) throws Exception;

  /**
   Delete a specified Entity

   @param access control
   @param id     of specific Entity to delete.
   */
  void destroy(Access access, BigInteger id) throws Exception;
}
