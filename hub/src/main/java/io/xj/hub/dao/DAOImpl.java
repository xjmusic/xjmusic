// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Text;
import io.xj.lib.util.Values;
import org.jooq.Record;
import org.jooq.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;

public abstract class DAOImpl<E> implements DAO<E> {
  private static final Logger log = LoggerFactory.getLogger(DAOImpl.class);
  private static final String KEY_ID = "id";
  protected final JsonapiPayloadFactory payloadFactory;
  protected final EntityFactory entityFactory;
  protected HubDatabaseProvider dbProvider;

  @Inject
  public DAOImpl(
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    this.payloadFactory = payloadFactory;
    this.entityFactory = entityFactory;
  }

  /**
   Execute a database CREATE operation

   @param <R>    record type dynamic
   @param db     DSL context
   @param table  to of entity in
   @param entity to of
   @return record
   */
  protected <R extends UpdatableRecord<R>> R executeCreate(DSLContext db, Table<R> table, E entity) throws DAOException, JsonapiException {
    R record = db.newRecord(table);
    setAll(record, entity);

    try {
      record.store();
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new DAOException(String.format("Cannot create record because %s", e.getMessage()));
    }

    return record;
  }

  /**
   Execute a database UPDATE operation@param <R>   record type dynamic
   @param db    context in which to execute
   @param table to update
   @param id    of record to update


   */
  protected <R extends UpdatableRecord<R>> void executeUpdate(DSLContext db, Table<R> table, UUID id, E entity) throws DAOException, JsonapiException {
    R record = db.newRecord(table);
    setAll(record, entity);
    set(record, KEY_ID, id);

    if (0 == db.executeUpdate(record))
      throw new DAOException("No records updated.");
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws DAOException if result set is not empty.
   @throws DAOException if something goes wrong.
   */
  protected <R extends Record> void requireNotExists(String name, Collection<R> result) throws DAOException {
    if (Values.isNonNull(result) && !result.isEmpty()) throw new DAOException("Found" + " " + name);
  }

  /**
   Require empty count of a Result

   @param name  to require
   @param count to check.
   @throws DAOException if result set is not empty.
   @throws DAOException if something goes wrong.
   */
  protected void requireNotExists(String name, int count) throws DAOException {
    if (0 < count) throw new DAOException("Found" + " " + name);
  }

  /**
   Require that a record isNonNull

   @param name   name of record (for error message)
   @param record to require existence of
   @throws DAOException if not isNonNull
   */
  protected <R extends Record> void requireExists(String name, R record) throws DAOException {
    require(name, "does not exist", Values.isNonNull(record));
  }

  /**
   Require that an entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws DAOException if not isNonNull
   */
  protected void requireExists(String name, E entity) throws DAOException {
    require(name, "does not exist", Values.isNonNull(entity));
  }

  /**
   Require that a count of a record isNonNull

   @param name  name of record (for error message)
   @param count to require existence of
   @throws DAOException if not isNonNull
   */
  protected void requireExists(String name, int count) throws DAOException {
    require(name, "does not exist", 0 < count);
  }

  /**
   Require user has admin hubAccess

   @param hubAccess control
   @throws DAOException if not admin
   */
  protected void requireTopLevel(HubAccess hubAccess) throws DAOException {
    require("top-level hubAccess", hubAccess.isTopLevel());
  }

  /**
   ASSUMED an entity.parentId() is a libraryId for this class of entity
   Require library-level hubAccess to an entity

   @param hubAccess control
   @throws DAOException if we do not have hub access
   */
  protected void requireArtist(HubAccess hubAccess) throws DAOException {
    require(hubAccess, UserRoleType.Artist);
  }

  /**
   Require hubAccess has one of the specified roles
   <p>
   Uses static formats to improve efficiency of method calls with less than 3 allowed roles

   @param hubAccess    to validate
   @param allowedRoles to require
   @throws DAOException if hubAccess does not have any one of the specified roles
   */
  protected void require(HubAccess hubAccess, UserRoleType... allowedRoles) throws DAOException {
    if (hubAccess.isTopLevel()) return;
    if (3 < allowedRoles.length)
      require(
        String.format("%s role", Arrays.stream(allowedRoles).map(Enum::toString).collect(Collectors.joining("/"))),
        hubAccess.isAllowed(allowedRoles));
    else if (2 < allowedRoles.length)
      require(String.format("%s/%s/%s role", allowedRoles[0], allowedRoles[1], allowedRoles[2]),
        hubAccess.isAllowed(allowedRoles));
    else if (1 < allowedRoles.length)
      require(String.format("%s/%s role", allowedRoles[0], allowedRoles[1]),
        hubAccess.isAllowed(allowedRoles));
    else if (0 < allowedRoles.length)
      require(String.format("%s role", allowedRoles[0]),
        hubAccess.isAllowed(allowedRoles));
    else throw new DAOException("No roles allowed.");
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws DAOException if not true
   */
  protected void require(String name, Boolean mustBeTrue) throws DAOException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param message    condition (for error message)
   @param mustBeTrue to require true
   @param condition  to append
   @throws DAOException if not true
   */
  protected void require(String message, String condition, Boolean mustBeTrue) throws DAOException {
    if (!mustBeTrue) throw new DAOException(message + " " + condition);
  }

  /**
   Require permission to modify the specified program

   @param db        context
   @param hubAccess control
   @param id        of entity to require modification hubAccess to
   @throws DAOException on invalid permissions
   */
  protected void requireProgramModification(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);

    if (hubAccess.isTopLevel())
      requireExists("Program", db.selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Program in Account you have hubAccess to", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   set all values from an entity onto a record

   @param target to set values on
   @param source to get value from
   @param <R>    type of record
   @param <N>    type of entity
   @throws JsonapiException on a REST API payload related failure to parse
   */
  protected <R extends Record, N> void setAll(R target, N source) throws JsonapiException {
    try {
      // start with all the entity resource attributes and their values
      Map<String, Object> attributes = entityFactory.getResourceAttributes(source);

      // add id for each belongs-to relationship
      for (String belongsTo : entityFactory.getBelongsTo(source.getClass().getSimpleName())) {
        String belongsToName = Entities.toIdAttribute(belongsTo);
        Entities.get(source, belongsToName).ifPresent(value -> attributes.put(belongsToName, value));
      }

      // set each attribute value
      for (Map.Entry<String, Object> entry : attributes.entrySet()) {
        String name = entry.getKey();
        Object value = entry.getValue();
        try {
          set(target, name, value);
        } catch (DAOException e) {
          log.error(String.format("Unable to set record %s attribute %s to value %s", target, name, value), e);
        }
      }
    } catch (EntityException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Get a value of a target object via attribute name

   @param attributeName of attribute to get
   @return value
   @throws DAOException on failure to get
   */
  protected <R extends Record> Optional<Object> get(R target, String attributeName) throws DAOException {
    String getterName = Entities.toGetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return Entities.get(target, method);

        } catch (EntityException e) {
          throw new DAOException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(target), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   Set a value using an attribute name

   @param attributeName of attribute for which to find setter method
   @param value         to set
   */
  protected <R extends Record> void set(R target, String attributeName, Object value) throws DAOException {
    if (Objects.isNull(value)) return;

    String setterName = Entities.toSetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          if (nullValueClasses.contains(value.getClass().getSimpleName()))
            setNull(target, method);
          else
            Entities.set(target, method, value);
          return;

        } catch (EntityException e) {
          throw new DAOException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));

        } catch (IllegalAccessException e) {
          throw new DAOException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));

        } catch (InvocationTargetException e) {
          throw new DAOException(String.format("Cannot invoke target %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));
        }

    // no op if setter does not exist
  }

  /**
   Set null value on target

   @param target to set on
   @param setter to set with
   @throws InvocationTargetException on failure to invoke setter
   @throws IllegalAccessException    on failure to access setter
   */
  protected <R extends Record> void setNull(R target, Method setter) throws InvocationTargetException, IllegalAccessException, DAOException {
    if (Objects.nonNull(setter.getAnnotation(Nullable.class)))
      setter.invoke(null);

    switch (Text.getSimpleName(setter.getParameterTypes()[0])) {
      case "String" -> setter.invoke(target, (String) null);
      case "Float" -> setter.invoke(target, (Float) null);
      case "Short" -> setter.invoke(target, (Short) null);
      case "Timestamp" -> setter.invoke(target, (Timestamp) null);
      default -> {
        log.error(String.format("Don't know how to set null value via %s on\n%s", setter, target));
        throw new DAOException(String.format("Don't know how to set null value via %s", setter));
      }
    }
  }

  /**
   Get a collection of records based ona collection of entities

   @param db       to make new records in
   @param table    to make new records in
   @param entities to source number of rows and their values for new records
   @param <R>      type of record (only one type in collection)
   @param <N>      type of entity (only one type in collection)
   @return collection of records
   @throws DAOException on failure
   */
  protected <R extends TableRecord<?>, N> Collection<R> recordsFrom(DSLContext db, Table<?> table, Collection<N> entities) throws DAOException, JsonapiException {
    Collection<R> records = Lists.newArrayList();
    for (N e : entities) {
      //noinspection unchecked
      R record = (R) db.newRecord(table);
      try {
        UUID id = Entities.getId(e);
        if (Objects.nonNull(id))
          set(record, KEY_ID, id);

      } catch (EntityException ignored) {

      }
      setAll(record, e);
      records.add(record);
    }
    return records;
  }

  @Override
  public <N, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws DAOException {
    Collection<N> models = Lists.newArrayList();
    for (R record : records) models.add(modelFrom(modelClass, record));
    return models;
  }

  @Override
  public <N, R extends Record> N modelFrom(R record) throws DAOException {
    if (!modelsForRecords.containsKey(record.getClass()))
      throw new DAOException(String.format("Unrecognized class of entity record: %s", record.getClass().getName()));

    //noinspection unchecked
    return modelFrom((Class<N>) modelsForRecords.get(record.getClass()), record);
  }

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param modelClass to whose setters the values will be written
   @param record     to source field-values of
   @return entity after transmogrification
   @throws DAOException on failure to transmogrify
   */
  protected <N, R extends Record> N modelFrom(Class<N> modelClass, R record) throws DAOException {
    if (Objects.isNull(modelClass))
      throw new DAOException("Will not transmogrify null modelClass");

    // new instance of model
    N model;
    try {
      model = entityFactory.getInstance(modelClass);
    } catch (Exception e) {
      throw new DAOException(String.format("Could not get a new instance create class %s because %s", modelClass, e));
    }

    // set all values
    modelSetTransmogrified(record, model);

    return model;
  }

  /**
   Set all fields of an Entity using values transmogrified of a jOOQ Record

   @param record to transmogrify values of
   @param model  to set fields of
   @throws DAOException on failure to set transmogrified values
   */
  protected <N, R extends Record> void modelSetTransmogrified(R record, N model) throws DAOException {
    if (Objects.isNull(record))
      throw new DAOException("Record does not exist");

    Map<String, Object> fieldValues = record.intoMap();
    for (Map.Entry<String, Object> field : fieldValues.entrySet())
      if (Values.isNonNull(field.getValue())) try {
        String attributeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field.getKey());
        Entities.set(model, attributeName, field.getValue());
      } catch (Exception e) {
        log.error("Could not transmogrify key:{} val:{} because {}", field.getKey(), field.getValue(), e);
        throw new DAOException(String.format("Could not transmogrify key:%s val:%s because %s", field.getKey(), field.getValue(), e));
      }
  }

  /**
   Get a table record for an entity

   @param <N>    type of entity
   @param db     database context
   @param entity to get table record for
   @return table record
   @throws DAOException on failure
   */
  protected <N> UpdatableRecord<?> recordFor(DSLContext db, N entity) throws DAOException, JsonapiException {
    Table<?> table = tablesInSchemaConstructionOrder.get(entity.getClass());
    var raw = db.newRecord(table);
    UpdatableRecord<?> record = (UpdatableRecord<?>) raw;

    try {
      UUID id = Entities.getId(entity);
      if (Objects.nonNull(id))
        set(record, KEY_ID, id);
    } catch (EntityException ignored) {
    }

    setAll(record, entity);

    return record;
  }

  /**
   Insert entity to database

   @param entity to insert
   @param db     database context
   @return the same entity (for chaining methods)
   */
  protected <N> N insert(DSLContext db, N entity) throws DAOException, JsonapiException {
    UpdatableRecord<?> record = recordFor(db, entity);
    record.store();

    try {
      UUID id = Entities.getId(entity);
      if (Objects.nonNull(id)) Entities.setId(record, id);
    } catch (EntityException ignored) {
    }

    return entity;
  }


  /**
   Require has engineer-level access

   @param access to validate
   @throws DAOException if not engineer
   */
  protected void requireEngineer(HubAccess access) throws DAOException {
    require(access, UserRoleType.Engineer);
  }

}
