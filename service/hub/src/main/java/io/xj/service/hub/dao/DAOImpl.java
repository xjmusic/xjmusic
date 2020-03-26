// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.lib.rest_api.PayloadEntity;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PROGRAM;

public abstract class DAOImpl<E extends Entity> implements DAO<E> {
  private static final Logger log = LoggerFactory.getLogger(DAOImpl.class);
  protected final PayloadFactory payloadFactory;
  protected SQLDatabaseProvider dbProvider;

  @Inject
  public DAOImpl(PayloadFactory payloadFactory) {
    this.payloadFactory = payloadFactory;
  }

  @Override
  public void createMany(Access access, Collection<E> entities) throws HubException, RestApiException, ValueException {
    for (E entity : entities) create(access, entity);
  }

  /**
   Execute a database CREATE operation

   @param <R>    record type dynamic
   @param db     DSL context
   @param table  to of entity in
   @param entity to of
   @return record
   */
  protected <R extends UpdatableRecord<R>> R executeCreate(DSLContext db, Table<R> table, E entity) throws HubException, RestApiException {
    R record = db.newRecord(table);
    setAll(record, entity);

    try {
      record.store();
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new HubException(String.format("Cannot create record because %s", e.getMessage()));
    }

    return record;
  }

  /**
   Execute a database UPDATE operation@param <R>        record type dynamic@param table      to update@param db

   @param id of record to update
   */
  protected <R extends UpdatableRecord<R>> void executeUpdate(DSLContext db, Table<R> table, UUID id, E entity) throws HubException, RestApiException {
    R record = db.newRecord(table);
    setAll(record, entity);
    set(record, "id", id);

    if (0 == db.executeUpdate(record))
      throw new HubException("No records updated.");
  }

  /**
   Require empty Result

   @param name   to require
   @param result to check.
   @throws HubException if result set is not empty.
   @throws HubException if something goes wrong.
   */
  protected <R extends Record> void requireNotExists(String name, Collection<R> result) throws HubException {
    if (Value.isNonNull(result) && !result.isEmpty()) {
      throw new HubException("Found" + " " + name);
    }
  }

  /**
   Require empty count of a Result

   @param name  to require
   @param count to check.
   @throws HubException if result set is not empty.
   @throws HubException if something goes wrong.
   */
  protected void requireNotExists(String name, int count) throws HubException {
    if (0 < count) {
      throw new HubException("Found" + " " + name);
    }
  }

  /**
   Require that a record isNonNull

   @param name   name of record (for error message)
   @param record to require existence of
   @throws HubException if not isNonNull
   */
  protected <R extends Record> void requireExists(String name, R record) throws HubException {
    require(name, "does not exist", Value.isNonNull(record));
  }

  /**
   Require that a entity isNonNull

   @param name   name of entity (for error message)
   @param entity to require existence of
   @throws HubException if not isNonNull
   */
  protected void requireExists(String name, E entity) throws HubException {
    require(name, "does not exist", Value.isNonNull(entity));
  }

  /**
   Require that a count of a record isNonNull

   @param name  name of record (for error message)
   @param count to require existence of
   @throws HubException if not isNonNull
   */
  protected void requireExists(String name, int count) throws HubException {
    require(name, "does not exist", 0 < count);
  }

  /**
   Require user has access to account #

   @param access    control
   @param accountId to check for access to
   @throws HubException if not admin
   */
  protected void requireAccount(Access access, UUID accountId) throws HubException {
    require("access to account #" + accountId, access.hasAccount(accountId));
  }

  /**
   Require user has admin access

   @param access control
   @throws HubException if not admin
   */
  protected void requireTopLevel(Access access) throws HubException {
    require("top-level access", access.isTopLevel());
  }

  /**
   Require has user-level access

   @param access control
   @throws HubException if not user
   */
  protected void requireUser(Access access) throws HubException {
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.USER))
      throw new HubException("No user access");
  }

  /**
   Require has engineer-level access

   @param access control
   @throws HubException if not engineer
   */
  protected void requireEngineer(Access access) throws HubException {
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.ENGINEER))
      throw new HubException("No engineer access");
  }

  /**
   ASSUMED an entity.parentId() is a libraryId for this class of entity
   Require library-level access to an entity

   @param access control
   @throws HubException if does not have access
   */
  protected void requireArtist(Access access) throws HubException {
    // TODO require a specific set of library ids, and check for access to all those libraries
    if (!access.isTopLevel() && !access.isAllowed(UserRoleType.ARTIST))
      throw new HubException("No artist access");
  }

  /**
   Require user has admin access

   @param access control
   @throws HubException if not admin
   */
  protected void requireRole(String message, Access access, UserRoleType... roles) throws HubException {
    require(message, access.isTopLevel() || access.isAllowed(roles));
  }

  /**
   Require that a condition is true, else error that it is required

   @param name       name of condition (for error message)
   @param mustBeTrue to require true
   @throws HubException if not true
   */
  protected void require(String name, Boolean mustBeTrue) throws HubException {
    require(name, "is required", mustBeTrue);
  }

  /**
   Require that a condition is true, else error that it is required

   @param message    condition (for error message)
   @param mustBeTrue to require true
   @param condition  to append
   @throws HubException if not true
   */
  protected void require(String message, String condition, Boolean mustBeTrue) throws HubException {
    if (!mustBeTrue) {
      throw new HubException(message + " " + condition);
    }
  }

  /**
   Execute a database CREATE operation@param <R>        record type dynamic@param db

   @param table    to of
   @param entities to batch insert
   */
  protected <R extends UpdatableRecord<R>> void executeCreateMany(DSLContext db, Table<R> table, Collection<E> entities) throws HubException, RestApiException {
    Collection<R> records = Lists.newArrayList();
    for (E entity : entities) {
      R record = db.newRecord(table);
      setAll(record, entity);
      // also set id if provided, creating a new record with that id
      if (Objects.nonNull(entity.getId()))
        set(record, "id", entity.getId());
      records.add(record);
    }

    try {
      db.batchInsert(records);
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new HubException(String.format("Cannot create record because %s", e.getMessage()));
    }
  }

  /**
   Require permission to modify the specified program

   @param db     context
   @param access control
   @param id     of entity to require modification access to
   @throws HubException on invalid permissions
   */
  protected void requireProgramModification(DSLContext db, Access access, UUID id) throws HubException {
    requireArtist(access);

    if (access.isTopLevel())
      requireExists("Program", db.selectCount().from(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(0, int.class));
    else
      requireExists("Program in Account you have access to", db.selectCount().from(PROGRAM)
        .join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PROGRAM.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));
  }

  /**
   set all values from an entity onto a record

   @param target to set values on
   @param source to get value from
   @param <R>    type of record
   @param <N>    type of entity
   @throws RestApiException on a REST API payload related failure to parse
   */
  protected <R extends Record, N extends Entity> void setAll(R target, N source) throws RestApiException {

    // start with all of the entity resource attributes and their values
    Map<String, Object> attributes = payloadFactory.getResourceAttributes(source);

    // add id for each belongs-to relationship
    for (String belongsTo : payloadFactory.getBelongsTo(source.getClass().getSimpleName())) {
      String belongsToName = Text.toIdAttribute(belongsTo);
      PayloadEntity.get(source, belongsToName).ifPresent(value -> attributes.put(belongsToName, value));
    }

    // set each attribute value
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      try {
        set(target, name, value);
      } catch (HubException e) {
        log.error(String.format("Unable to set record %s attribute %s to value %s", target, name, value), e);
      }
    }


  }

  /**
   Get a value of a target object via attribute name

   @param attributeName of attribute to get
   @return value
   @throws HubException on failure to get
   */
  protected <R extends Record> Optional<Object> get(R target, String attributeName) throws HubException {
    String getterName = Text.toGetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return PayloadEntity.get(target, method);

        } catch (InvocationTargetException e) {
          throw new HubException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(target), getterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new HubException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(target), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   Set a value using an attribute name

   @param attributeName of attribute for which to find setter method
   @param value         to set
   */
  protected <R extends Record> void set(R target, String attributeName, Object value) throws HubException {
    if (Objects.isNull(value)) return;

    String setterName = Text.toSetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          if (nullValueClasses.contains(value.getClass().getSimpleName()))
            setNull(target, method);
          else
            PayloadEntity.set(target, method, value);
          return;

        } catch (InvocationTargetException e) {
          throw new HubException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new HubException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));

        } catch (NoSuchMethodException e) {
          throw new HubException(String.format("No such method %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));
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
  protected <R extends Record> void setNull(R target, Method setter) throws InvocationTargetException, IllegalAccessException, HubException {
    if (Objects.nonNull(setter.getAnnotation(Nullable.class)))
      setter.invoke(null);

    switch (Text.getSimpleName(setter.getParameterTypes()[0])) {
      case "String":
        setter.invoke(target, (String) null);
        break;

      case "Float":
        setter.invoke(target, (Float) null);
        break;

      case "Short":
        setter.invoke(target, (Short) null);
        break;

      case "Timestamp":
        setter.invoke(target, (Timestamp) null);
        break;

      default:
        log.error(String.format("Don't know how to set null value via %s on\n%s", setter, target));
        throw new HubException(String.format("Don't know how to set null value via %s", setter));
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
   @throws HubException on failure
   */
  protected <R extends TableRecord<?>, N extends Entity> Collection<R> recordsFrom(DSLContext db, Table<?> table, Collection<N> entities) throws HubException, RestApiException {
    Collection<R> records = Lists.newArrayList();
    for (N e : entities) {
      //noinspection unchecked
      R record = (R) db.newRecord(table);
      if (Objects.nonNull(e.getId()))
        set(record, "id", e.getId());
      setAll(record, e);
      records.add(record);
    }
    return records;
  }

  @Override
  public <N extends Entity, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws HubException {
    Collection<N> models = Lists.newArrayList();
    for (R record : records) models.add(modelFrom(modelClass, record));
    return models;
  }

  @Override
  public <N extends Entity, R extends Record> N modelFrom(R record) throws HubException {
    if (!modelsForRecords.containsKey(record.getClass()))
      throw new HubException(String.format("Unrecognized class of entity record: %s", record.getClass().getName()));

    //noinspection unchecked
    return (N) modelFrom(modelsForRecords.get(record.getClass()), record);
  }

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param modelClass to whose setters the values will be written
   @param record     to source field-values of
   @return entity after transmogrification
   @throws HubException on failure to transmogrify
   */
  protected <N extends Entity, R extends Record> N modelFrom(Class<N> modelClass, R record) throws HubException {
    if (Objects.isNull(modelClass))
      throw new HubException("Will not transmogrify null modelClass");

    // new instance of model
    N model;
    try {
      model = modelClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new HubException(String.format("Could not get a new instance create class %s because %s", modelClass, e));
    }

    // set all values
    modelSetTransmogrified(record, model);

    return model;
  }

  /**
   Set all fields of an Entity using values transmogrified of a jOOQ Record

   @param record to transmogrify values of
   @param model  to set fields of
   @throws HubException on failure to set transmogrified values
   */
  protected <N extends Entity, R extends Record> void modelSetTransmogrified(R record, N model) throws HubException {
    if (Objects.isNull(record))
      throw new HubException("Record does not exist");

    Map<String, Object> fieldValues = record.intoMap();
    for (Map.Entry<String, Object> field : fieldValues.entrySet())
      if (Value.isNonNull(field.getValue())) try {
        String attributeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field.getKey());
        PayloadEntity.set(model, attributeName, field.getValue());
      } catch (Exception e) {
        log.error("Could not transmogrify key:{} val:{} because {}", field.getKey(), field.getValue(), e);
        throw new HubException(String.format("Could not transmogrify key:%s val:%s because %s", field.getKey(), field.getValue(), e));
      }
  }

  /**
   Get a table record for an entity

   @param <N>    type of entity
   @param db     database context
   @param entity to get table record for
   @return table record
   @throws HubException on failure
   */
  protected <N extends Entity> UpdatableRecord<?> recordFor(DSLContext db, N entity) throws HubException, RestApiException {
    Table<?> table = tablesInSchemaConstructionOrder.get(entity.getClass());
    UpdatableRecord<?> record = (UpdatableRecord<?>) db.newRecord(table);

    if (Objects.nonNull(entity.getCreatedAt()))
      set(record, "createdAt", Timestamp.from(entity.getCreatedAt()));

    if (Objects.nonNull(entity.getUpdatedAt()))
      set(record, "updatedAt", Timestamp.from(entity.getUpdatedAt()));

    setAll(record, entity);

    return record;
  }

  /**
   Insert Chain to database

   @param entity to insert
   @param db     database context
   @return the same chain (for chaining methods)
   */
  protected <N extends Entity> N insert(DSLContext db, N entity) throws HubException, RestApiException {
    UpdatableRecord<?> record = recordFor(db, entity);
    record.store();
    get(record, "id").ifPresent(id ->
      entity.setId(UUID.fromString(String.valueOf(id))));
    return entity;
  }

}
