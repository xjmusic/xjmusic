// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache;

import io.xj.core.access.Access;
import io.xj.core.dao.DAO;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

import java.util.Collection;
import java.util.UUID;

/**
 Wraps a DAO to cache its results

 @param <E> type of Entity
 @param <D> type of DAO */
public class DAOEntityCache<E extends Entity, D extends DAO<E>> extends EntityCache<E> {
  private final Access access;
  private final D dao;

  /**
   Construct a new DAO Entity Cache

   @param access control
   @param dao    to ingest
   */
  public DAOEntityCache(Access access, D dao) {
    this.access = access;
    this.dao = dao;
  }

  /**
   @return DAO
   */
  public D getDAO() {
    return dao;
  }

  /**
   Write the added entities to the database and then clear the added entities queue
   such as to do the writing of those entities to the database only once
   */
  public void writeInserts() throws CoreException {
    dao.createMany(access, flushInserts());
  }

  /**
   @param ids to fetch
   @throws CoreException on failure
   */
  public void fetchAll(Collection<UUID> ids) throws CoreException {
    for (UUID id : ids) add(dao.readOne(access, id));
  }

  /**
   @param parentIds to fetch children of
   @throws CoreException on failure
   */
  public void fetchAllBelongingTo(Collection<UUID> parentIds) throws CoreException {
    dao.readMany(access, parentIds).forEach(this::add);
  }
}
