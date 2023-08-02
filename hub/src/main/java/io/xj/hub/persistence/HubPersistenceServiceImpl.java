// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.persistence;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.UserRoleType;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.hub.tables.records.AccountRecord;
import io.xj.hub.tables.records.AccountUserRecord;
import io.xj.hub.tables.records.InstrumentAudioRecord;
import io.xj.hub.tables.records.InstrumentMemeRecord;
import io.xj.hub.tables.records.InstrumentRecord;
import io.xj.hub.tables.records.LibraryRecord;
import io.xj.hub.tables.records.ProgramMemeRecord;
import io.xj.hub.tables.records.ProgramRecord;
import io.xj.hub.tables.records.ProgramSequenceBindingMemeRecord;
import io.xj.hub.tables.records.ProgramSequenceBindingRecord;
import io.xj.hub.tables.records.ProgramSequenceChordRecord;
import io.xj.hub.tables.records.ProgramSequenceChordVoicingRecord;
import io.xj.hub.tables.records.ProgramSequencePatternEventRecord;
import io.xj.hub.tables.records.ProgramSequencePatternRecord;
import io.xj.hub.tables.records.ProgramSequenceRecord;
import io.xj.hub.tables.records.ProgramVoiceRecord;
import io.xj.hub.tables.records.ProgramVoiceTrackRecord;
import io.xj.hub.tables.records.TemplateBindingRecord;
import io.xj.hub.tables.records.TemplatePlaybackRecord;
import io.xj.hub.tables.records.TemplatePublicationRecord;
import io.xj.hub.tables.records.TemplateRecord;
import io.xj.hub.tables.records.UserAuthRecord;
import io.xj.hub.tables.records.UserAuthTokenRecord;
import io.xj.hub.tables.records.UserRecord;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.hub.Tables.ACCOUNT;
import static io.xj.hub.Tables.ACCOUNT_USER;
import static io.xj.hub.Tables.INSTRUMENT;
import static io.xj.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.hub.Tables.INSTRUMENT_MEME;
import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.hub.Tables.PROGRAM_VOICE;
import static io.xj.hub.Tables.PROGRAM_VOICE_TRACK;
import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_BINDING;
import static io.xj.hub.Tables.TEMPLATE_PLAYBACK;
import static io.xj.hub.Tables.TEMPLATE_PUBLICATION;
import static io.xj.hub.Tables.USER;
import static io.xj.hub.Tables.USER_AUTH;
import static io.xj.hub.Tables.USER_AUTH_TOKEN;

@Service
public class HubPersistenceServiceImpl {
  static final Logger log = LoggerFactory.getLogger(HubPersistenceServiceImpl.class);
  static final String KEY_ID = "id";
  protected final EntityFactory entityFactory;
  protected final HubSqlStoreProvider sqlStoreProvider;
  protected Collection<ClassSchemaPair> tablesInSchemaConstructionOrder = buildTablesInSchemaConstructionOrder();

  protected Map<Class<? extends Record>, Class<?>> modelsForRecords = buildModelsForRecords();

  private Map<Class<? extends Record>, Class<?>> buildModelsForRecords() {
    Map<Class<? extends Record>, Class<?>> map = new HashMap<>();
    map.put(UserRecord.class, User.class);
    map.put(UserAuthRecord.class, UserAuth.class);
    map.put(UserAuthTokenRecord.class, UserAuthToken.class);
    map.put(AccountRecord.class, Account.class);
    map.put(AccountUserRecord.class, AccountUser.class);
    map.put(TemplateRecord.class, Template.class);
    map.put(TemplateBindingRecord.class, TemplateBinding.class);
    map.put(TemplatePlaybackRecord.class, TemplatePlayback.class);
    map.put(TemplatePublicationRecord.class, TemplatePublication.class);
    map.put(LibraryRecord.class, Library.class);
    map.put(ProgramRecord.class, Program.class);
    map.put(ProgramMemeRecord.class, ProgramMeme.class);
    map.put(ProgramVoiceRecord.class, ProgramVoice.class);
    map.put(ProgramVoiceTrackRecord.class, ProgramVoiceTrack.class);
    map.put(ProgramSequenceRecord.class, ProgramSequence.class);
    map.put(ProgramSequenceBindingRecord.class, ProgramSequenceBinding.class);
    map.put(ProgramSequenceBindingMemeRecord.class, ProgramSequenceBindingMeme.class);
    map.put(ProgramSequenceChordRecord.class, ProgramSequenceChord.class);
    map.put(ProgramSequenceChordVoicingRecord.class, ProgramSequenceChordVoicing.class);
    map.put(ProgramSequencePatternRecord.class, ProgramSequencePattern.class);
    map.put(ProgramSequencePatternEventRecord.class, ProgramSequencePatternEvent.class);
    map.put(InstrumentRecord.class, Instrument.class);
    map.put(InstrumentAudioRecord.class, InstrumentAudio.class);
    map.put(InstrumentMemeRecord.class, InstrumentMeme.class);
    return map;
  }

  /**
   * This is in a very DELIBERATE ORDER: the order in which the tables are created in the database.
   *
   * @return a map of the tables in the order in which they are created in the database.
   */
  static Collection<ClassSchemaPair> buildTablesInSchemaConstructionOrder() {
    Collection<ClassSchemaPair> pairs = new ArrayList<>();
    pairs.add(new ClassSchemaPair(io.xj.hub.tables.pojos.User.class, USER)); // first
    pairs.add(new ClassSchemaPair(UserAuth.class, USER_AUTH)); // after user
    pairs.add(new ClassSchemaPair(UserAuthToken.class, USER_AUTH_TOKEN)); // after user
    pairs.add(new ClassSchemaPair(Account.class, ACCOUNT)); // after user
    pairs.add(new ClassSchemaPair(AccountUser.class, ACCOUNT_USER)); // after user
    pairs.add(new ClassSchemaPair(Template.class, TEMPLATE)); // after account
    pairs.add(new ClassSchemaPair(TemplateBinding.class, TEMPLATE_BINDING)); // after template
    pairs.add(new ClassSchemaPair(TemplatePlayback.class, TEMPLATE_PLAYBACK)); // after template
    pairs.add(new ClassSchemaPair(TemplatePublication.class, TEMPLATE_PUBLICATION)); // after template
    pairs.add(new ClassSchemaPair(Library.class, LIBRARY)); // after account
    pairs.add(new ClassSchemaPair(Program.class, PROGRAM)); // after library
    pairs.add(new ClassSchemaPair(ProgramMeme.class, PROGRAM_MEME));
    pairs.add(new ClassSchemaPair(ProgramVoice.class, PROGRAM_VOICE));
    pairs.add(new ClassSchemaPair(ProgramVoiceTrack.class, PROGRAM_VOICE_TRACK));
    pairs.add(new ClassSchemaPair(ProgramSequence.class, PROGRAM_SEQUENCE));
    pairs.add(new ClassSchemaPair(ProgramSequenceBinding.class, PROGRAM_SEQUENCE_BINDING));
    pairs.add(new ClassSchemaPair(ProgramSequenceBindingMeme.class, PROGRAM_SEQUENCE_BINDING_MEME));
    pairs.add(new ClassSchemaPair(ProgramSequenceChord.class, PROGRAM_SEQUENCE_CHORD));
    pairs.add(new ClassSchemaPair(ProgramSequenceChordVoicing.class, PROGRAM_SEQUENCE_CHORD_VOICING));
    pairs.add(new ClassSchemaPair(ProgramSequencePattern.class, PROGRAM_SEQUENCE_PATTERN));
    pairs.add(new ClassSchemaPair(ProgramSequencePatternEvent.class, PROGRAM_SEQUENCE_PATTERN_EVENT));
    pairs.add(new ClassSchemaPair(Instrument.class, INSTRUMENT)); // after library
    pairs.add(new ClassSchemaPair(InstrumentAudio.class, INSTRUMENT_AUDIO));
    pairs.add(new ClassSchemaPair(InstrumentMeme.class, INSTRUMENT_MEME));
    return pairs;
  }

  Collection<String> nullValueClasses = List.of("Null", "JsonNull");

  public HubPersistenceServiceImpl(EntityFactory entityFactory, HubSqlStoreProvider sqlStoreProvider) {
    this.entityFactory = entityFactory;
    this.sqlStoreProvider = sqlStoreProvider;
  }

  <R extends TableRecord<?>, N> void getEntities(N e, R record) throws ManagerException {
    try {
      UUID id = Entities.getId(e);
      if (Objects.nonNull(id))
        set(record, KEY_ID, id);
    } catch (EntityException ignored) {
      // no op
    }
    try {
      setAll(record, e);
    } catch (HubPersistenceException e2) {
      throw new ManagerException(e2);
    }
  }

  public <N, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws ManagerException {
    Collection<N> models = new ArrayList<>();
    for (R record : records) models.add(modelFrom(modelClass, record));
    return models;
  }

  public <N, R extends Record> N modelFrom(R record) throws ManagerException {
    if (!modelsForRecords.containsKey(record.getClass()))
      throw new ManagerException(String.format("Unrecognized class of entity record: %s", record.getClass().getName()));

    //noinspection unchecked
    return modelFrom((Class<N>) modelsForRecords.get(record.getClass()), record);
  }

  /**
   * Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.
   *
   * @param modelClass to whose setters the values will be written
   * @param record     to source field-values of
   * @return entity after transmogrification
   * @throws ManagerException on failure to transmogrify
   */
  protected <N, R extends Record> N modelFrom(Class<N> modelClass, R record) throws ManagerException {
    if (Objects.isNull(modelClass))
      throw new ManagerException("Will not transmogrify null modelClass");

    // new instance of model
    N model;
    try {
      model = entityFactory.getInstance(modelClass);
    } catch (Exception e) {
      throw new ManagerException(String.format("Could not get a new instance create class %s because %s", modelClass, e));
    }

    // set all values
    modelSetTransmogrified(record, model);

    return model;
  }

  /**
   * Set all fields of an Entity using values transmogrified of a jOOQ Record
   *
   * @param record to transmogrify values of
   * @param model  to set fields of
   * @throws ManagerException on failure to set transmogrified values
   */
  protected <N, R extends Record> void modelSetTransmogrified(R record, N model) throws ManagerException {
    if (Objects.isNull(record))
      throw new ManagerException("Record does not exist");

    Map<String, Object> fieldValues = record.intoMap();
    for (Map.Entry<String, Object> field : fieldValues.entrySet())
      if (ValueUtils.isNonNull(field.getValue())) try {
        String attributeName = StringUtils.snakeToUpperCamelCase(field.getKey());
        Entities.set(model, attributeName, field.getValue());
      } catch (Exception e) {
        log.error("Could not transmogrify key:{} val:{} because {}", field.getKey(), field.getValue(), e);
        throw new ManagerException(String.format("Could not transmogrify key:%s val:%s because %s", field.getKey(), field.getValue(), e));
      }
  }

  /**
   * Get a table record for an entity
   *
   * @param <N>    type of entity
   * @param db     database context
   * @param entity to get table record for
   * @return table record
   * @throws ManagerException on failure
   */
  protected <N> UpdatableRecord<?> recordFor(DSLContext db, N entity) throws ManagerException {
    Table<?> table = tablesInSchemaConstructionOrder.stream().filter((t) -> t.tableClass.equals(entity.getClass())).findFirst().orElseThrow().table;
    var raw = db.newRecord(table);
    UpdatableRecord<?> record = (UpdatableRecord<?>) raw;

    getEntities(entity, record);

    return record;
  }

  /**
   * Insert entity to database
   *
   * @param entity to insert
   * @param db     database context
   * @return the same entity (for chaining methods)
   */
  protected <N> N insert(DSLContext db, N entity) throws ManagerException {
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
   * Require has engineer-level access
   *
   * @param access to validate
   * @throws ManagerException if not engineer
   */
  protected void requireEngineer(HubAccess access) throws ManagerException {
    requireAny(access, UserRoleType.Engineer);
  }

  /**
   * Execute a database CREATE operation
   *
   * @param <R>    record type dynamic
   * @param db     DSL context
   * @param table  to of entity in
   * @param entity to of
   * @return record
   */
  protected <R extends UpdatableRecord<R>, E> R executeCreate(DSLContext db, Table<R> table, E entity) throws ManagerException {
    R record = db.newRecord(table);

    try {
      setAll(record, entity);
      record.store();
    } catch (Exception e) {
      log.error("Cannot create record because {}", e.getMessage());
      throw new ManagerException(String.format("Cannot create record because %s", e.getMessage()));
    }

    return record;
  }

  /**
   * Execute a database UPDATE operation@param <R>   record type dynamic
   *
   * @param db    context in which to execute
   * @param table to update
   * @param id    of record to update
   */
  protected <R extends UpdatableRecord<R>, E> void executeUpdate(DSLContext db, Table<R> table, UUID id, E entity) throws ManagerException {
    R record = db.newRecord(table);
    try {
      setAll(record, entity);
    } catch (HubPersistenceException e) {
      throw new ManagerException("Failure to serialize");
    }
    set(record, KEY_ID, id);
    try {
      Entities.set(entity, KEY_ID, id);
    } catch (EntityException e) {
      throw new ManagerException("Failure to ensure correct id is returned");
    }

    if (0 == db.executeUpdate(record))
      throw new ManagerException("No records updated.");
  }

  /**
   * Require empty Result
   *
   * @param message to require
   * @param result  to check.
   * @throws ManagerException if result set is not empty.
   * @throws ManagerException if something goes wrong.
   */
  protected <R extends Record> void requireNotExists(String message, Collection<R> result) throws ManagerException {
    if (ValueUtils.isNonNull(result) && !result.isEmpty()) throw new ManagerException(message);
  }

  /**
   * Require empty count of a Result
   *
   * @param name  to require
   * @param count to check.
   * @throws ManagerException if result set is not empty.
   * @throws ManagerException if something goes wrong.
   */
  protected void requireNotExists(String name, @Nullable Integer count) throws ManagerException {
    if (Objects.isNull(count) || 0 < count) throw new ManagerException("Found" + " " + name);
  }

  /**
   * Require that a record isNonNull
   *
   * @param name   name of record (for error message)
   * @param record to require existence of
   * @throws ManagerException if not isNonNull
   */
  protected <R extends Record> void requireExists(String name, R record) throws ManagerException {
    requireAny(name, "does not exist", ValueUtils.isNonNull(record));
  }

  /**
   * Require that an entity isNonNull
   *
   * @param name   name of entity (for error message)
   * @param entity to require existence of
   * @throws ManagerException if not isNonNull
   */
  protected <E> void requireExists(String name, E entity) throws ManagerException {
    requireAny(name, "does not exist", ValueUtils.isNonNull(entity));
  }

  /**
   * Require that a count of a record isNonNull
   *
   * @param name  name of record (for error message)
   * @param count to require existence of
   * @throws ManagerException if not isNonNull
   */
  protected void requireExists(String name, @Nullable Integer count) throws ManagerException {
    requireAny(name, "does not exist", Objects.nonNull(count) && 0 < count);
  }

  /**
   * Require user has admin access
   *
   * @param access control
   * @throws ManagerException if not admin
   */
  protected void requireTopLevel(HubAccess access) throws ManagerException {
    requireAny("top-level access", access.isTopLevel());
  }

  /**
   * ASSUMED an entity.parentId() is a libraryId for this class of entity
   * Require library-level access to an entity
   *
   * @param access control
   * @throws ManagerException if we do not have hub access
   */
  protected void requireArtist(HubAccess access) throws ManagerException {
    requireAny(access, UserRoleType.Artist);
  }

  /**
   * Require access has one of the specified roles
   * <p>
   * Uses static formats to improve efficiency of method calls with less than 3 allowed roles
   *
   * @param access       to validate
   * @param allowedRoles to require
   * @throws ManagerException if access does not have any one of the specified roles
   */
  protected void requireAny(HubAccess access, UserRoleType... allowedRoles) throws ManagerException {
    if (access.isTopLevel()) return;

    if (0 == allowedRoles.length)
      throw new ManagerException("No roles allowed.");

    requireAny(
      String.format("%s role", Arrays.stream(allowedRoles).map(Enum::toString).collect(Collectors.joining("/"))),
      access.isAnyAllowed(allowedRoles));
  }

  /**
   * Require that a condition is true, else error that it is required
   *
   * @param name       name of condition (for error message)
   * @param mustBeTrue to require true
   * @throws ManagerException if not true
   */
  protected void requireAny(String name, Boolean mustBeTrue) throws ManagerException {
    requireAny(name, "is required", mustBeTrue);
  }

  /**
   * Require that a condition is true, else error that it is required
   *
   * @param message    condition (for error message)
   * @param mustBeTrue to require true
   * @param condition  to append
   * @throws ManagerException if not true
   */
  protected void requireAny(String message, String condition, Boolean mustBeTrue) throws ManagerException {
    if (!mustBeTrue) throw new ManagerException(message + " " + condition);
  }

  /**
   * Require permission to modify the specified program
   *
   * @param db     context
   * @param access control
   * @param id     of entity to require modification access to
   * @throws ManagerException on invalid permissions
   */
  protected void requireProgramModification(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);

    if (access.isTopLevel()) try (var selectCount = db.selectCount()) {
      requireExists("Program", selectCount.from(PROGRAM)
        .where(PROGRAM.ID.eq(id))
        .fetchOne(0, int.class));
    }
    else try (
      var selectCount = db.selectCount();
      var joinLibrary = selectCount.from(PROGRAM).join(LIBRARY).on(PROGRAM.LIBRARY_ID.eq(LIBRARY.ID))
    ) {
      requireExists("Program in Account you have access to",
        joinLibrary
          .where(PROGRAM.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));
    }
  }

  /**
   * set all values from an entity onto a record
   *
   * @param target to set values on
   * @param source to get value from
   * @param <R>    type of record
   * @param <N>    type of entity
   * @throws HubPersistenceException on a REST API payload related failure to parse
   */
  protected <R extends Record, N> void setAll(R target, N source) throws HubPersistenceException {
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
        } catch (ManagerException e) {
          log.error(String.format("Unable to set record %s attribute %s to value %s", target, name, value), e);
        }
      }
    } catch (EntityException e) {
      throw new HubPersistenceException(e);
    }
  }

  /**
   * Get a value of a target object via attribute name
   *
   * @param attributeName of attribute to get
   * @return value
   * @throws ManagerException on failure to get
   */
  protected <R extends Record> Optional<Object> get(R target, String attributeName) throws ManagerException {
    String getterName = Entities.toGetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return Entities.get(target, method);

        } catch (EntityException e) {
          throw new ManagerException(String.format("Failed to %s.%s(), reason: %s", StringUtils.getSimpleName(target), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   * Set a value using an attribute name
   *
   * @param attributeName of attribute for which to find setter method
   * @param value         to set
   */
  protected <R extends Record> void set(R target, String attributeName, Object value) throws ManagerException {
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
          throw new ManagerException(String.format("Failed to %s.%s(), reason: %s", StringUtils.getSimpleName(target), setterName, e.getMessage()));

        } catch (IllegalAccessException e) {
          throw new ManagerException(String.format("Could not access %s.%s(), reason: %s", StringUtils.getSimpleName(target), setterName, e.getMessage()));

        } catch (InvocationTargetException e) {
          throw new ManagerException(String.format("Cannot invoke target %s.%s(), reason: %s", StringUtils.getSimpleName(target), setterName, e.getMessage()));
        }

    // no op if setter does not exist
  }

  /**
   * Set null value on target
   *
   * @param target to set on
   * @param setter to set with
   * @throws InvocationTargetException on failure to invoke setter
   * @throws IllegalAccessException    on failure to access setter
   */
  protected <R extends Record> void setNull(R target, Method setter) throws InvocationTargetException, IllegalAccessException, ManagerException {
    if (Objects.nonNull(setter.getAnnotation(Nullable.class)))
      setter.invoke(null);

    switch (StringUtils.getSimpleName(setter.getParameterTypes()[0])) {
      case "String" -> setter.invoke(target, (String) null);
      case "Float" -> setter.invoke(target, (Float) null);
      case "Short" -> setter.invoke(target, (Short) null);
      case "Timestamp" -> setter.invoke(target, (Timestamp) null);
      default -> {
        log.error(String.format("Don't know how to set null value via %s on\n%s", setter, target));
        throw new ManagerException(String.format("Don't know how to set null value via %s", setter));
      }
    }
  }

  public record ClassSchemaPair(Class<?> tableClass, Table<?> table) {
  }

}
