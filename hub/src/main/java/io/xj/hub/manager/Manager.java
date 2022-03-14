// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

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

public interface Manager<E> {
  Logger log = LoggerFactory.getLogger(Manager.class);

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

   @param access control
   @param entity    for the new Record
   @return newly readMany record
   */
  E create(HubAccess access, E entity) throws ManagerException, JsonapiException, ValueException;

  /**
   Delete a specified Entity@param access control@param id of specific Entity to delete.
   */
  void destroy(HubAccess access, UUID id) throws ManagerException;

  /**
   Create a new instance of this type of Entity

   @return new entity instance
   */
  E newInstance();

  /**
   Fetch many records for many parents by id, if accessible

   @param access control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws ManagerException on failure
   */
  Collection<E> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException;

  /**
   Fetch one record  if accessible

   @param access control
   @param id        of record to fetch
   @return retrieved record
   @throws ManagerException on failure
   */
  E readOne(HubAccess access, UUID id) throws ManagerException;

  /**
   Update a specified Entity

   @param access control
   @param id        of specific Entity to update.
   @param entity    for the updated Entity.
   */
  E update(HubAccess access, UUID id, E entity) throws ManagerException, JsonapiException, ValueException;

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param modelClass instance of a single target entity
   @param records    to source values of
   @return entity after transmogrification
   @throws ManagerException on failure to transmogrify
   */
  <N, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws ManagerException;

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param record to source field-values of
   @return entity after transmogrification
   @throws ManagerException on failure to transmogrify
   */
  <N, R extends Record> N modelFrom(R record) throws ManagerException;

}
