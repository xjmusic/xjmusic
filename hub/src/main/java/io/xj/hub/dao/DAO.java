// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import io.xj.hub.access.HubAccess;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.UUID;

public interface DAO<E> {
  Logger log = LoggerFactory.getLogger(DAO.class);

  /**
   ids of a result set

   @param records set
   @return ids
   */
  static Collection<UUID> idsFrom(Result<Record1<UUID>> records) {
    return records.map(Record1::value1);
  }

  /**
   Get DSL context

   @param dataSource SQL dataSource
   @return DSL context
   */
  static DSLContext DSL(DataSource dataSource) {
    return DSL.using(dataSource, SQLDialect.POSTGRES, new Settings());
  }

  /**
   Create a new Record
   <p>
   [#175213519] Expect new Audios to have no waveform

   @param hubAccess control
   @param entity    for the new Record
   @return newly readMany record
   */
  E create(HubAccess hubAccess, E entity) throws DAOException, JsonapiException, ValueException;

  /**
   Delete a specified Entity@param hubAccess control@param id of specific Entity to delete.
   */
  void destroy(HubAccess hubAccess, UUID id) throws DAOException;

  /**
   Create a new instance of this type of Entity

   @return new entity instance
   */
  E newInstance();

  /**
   Fetch many records for many parents by id, if accessible

   @param hubAccess control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws DAOException on failure
   */
  Collection<E> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException;

  /**
   Fetch one record  if accessible

   @param hubAccess control
   @param id        of record to fetch
   @return retrieved record
   @throws DAOException on failure
   */
  E readOne(HubAccess hubAccess, UUID id) throws DAOException;

  /**
   Update a specified Entity

   @param hubAccess control
   @param id        of specific Entity to update.
   @param entity    for the updated Entity.
   */
  E update(HubAccess hubAccess, UUID id, E entity) throws DAOException, JsonapiException, ValueException;

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param modelClass instance of a single target entity
   @param records    to source values of
   @return entity after transmogrification
   @throws DAOException on failure to transmogrify
   */
  <N, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws DAOException;

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param record to source field-values of
   @return entity after transmogrification
   @throws DAOException on failure to transmogrify
   */
  <N, R extends Record> N modelFrom(R record) throws DAOException;

}
