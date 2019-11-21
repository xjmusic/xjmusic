package io.xj.core.dao;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.xj.core.dao.impl.DAOImpl;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.AccountUser;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.Library;
import io.xj.core.model.PlatformMessage;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentMessage;
import io.xj.core.model.User;
import io.xj.core.model.UserAuth;
import io.xj.core.model.UserAuthToken;
import io.xj.core.model.UserRole;
import io.xj.core.util.Text;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.ACCOUNT_USER;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHAIN_BINDING;
import static io.xj.core.Tables.CHAIN_CONFIG;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_AUDIO;
import static io.xj.core.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.core.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PLATFORM_MESSAGE;
import static io.xj.core.Tables.PROGRAM_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.core.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.core.Tables.PROGRAM_VOICE;
import static io.xj.core.Tables.PROGRAM_VOICE_TRACK;
import static io.xj.core.Tables.SEGMENT;
import static io.xj.core.Tables.SEGMENT_CHOICE;
import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static io.xj.core.Tables.SEGMENT_CHORD;
import static io.xj.core.Tables.SEGMENT_MEME;
import static io.xj.core.Tables.SEGMENT_MESSAGE;
import static io.xj.core.Tables.USER;
import static io.xj.core.Tables.USER_AUTH;
import static io.xj.core.Tables.USER_AUTH_TOKEN;
import static io.xj.core.Tables.USER_ROLE;
import static io.xj.core.tables.Program.PROGRAM;

public enum DAORecord {
  ;
  public static final Map<Class, Table> tablesInSchemaConstructionOrder = ImmutableMap.<Class, Table>builder() // DELIBERATE ORDER
    .put(User.class, USER)
    .put(UserRole.class, USER_ROLE) // after user
    .put(UserAuth.class, USER_AUTH) // after user
    .put(UserAuthToken.class, USER_AUTH_TOKEN) // after user
    .put(Account.class, ACCOUNT) // after user
    .put(AccountUser.class, ACCOUNT_USER) // after user
    .put(Library.class, LIBRARY) // after account
    .put(Program.class, PROGRAM) // after library
    .put(ProgramMeme.class, PROGRAM_MEME)
    .put(ProgramVoice.class, PROGRAM_VOICE)
    .put(ProgramVoiceTrack.class, PROGRAM_VOICE_TRACK)
    .put(ProgramSequence.class, PROGRAM_SEQUENCE)
    .put(ProgramSequenceBinding.class, PROGRAM_SEQUENCE_BINDING)
    .put(ProgramSequenceBindingMeme.class, PROGRAM_SEQUENCE_BINDING_MEME)
    .put(ProgramSequenceChord.class, PROGRAM_SEQUENCE_CHORD)
    .put(ProgramSequencePattern.class, PROGRAM_SEQUENCE_PATTERN)
    .put(ProgramSequencePatternEvent.class, PROGRAM_SEQUENCE_PATTERN_EVENT)
    .put(Instrument.class, INSTRUMENT) // after library
    .put(InstrumentAudio.class, INSTRUMENT_AUDIO)
    .put(InstrumentAudioChord.class, INSTRUMENT_AUDIO_CHORD)
    .put(InstrumentAudioEvent.class, INSTRUMENT_AUDIO_EVENT)
    .put(InstrumentMeme.class, INSTRUMENT_MEME)
    .put(Chain.class, CHAIN) // after account
    .put(ChainBinding.class, CHAIN_BINDING) // after chain, library
    .put(ChainConfig.class, CHAIN_CONFIG) // after chain
    .put(Segment.class, SEGMENT) // after chain
    .put(SegmentChord.class, SEGMENT_CHORD) // after segment
    .put(SegmentMeme.class, SEGMENT_MEME) // after segment
    .put(SegmentMessage.class, SEGMENT_MESSAGE) // after segment
    .put(SegmentChoice.class, SEGMENT_CHOICE) // after segment
    .put(SegmentChoiceArrangement.class, SEGMENT_CHOICE_ARRANGEMENT) // after segment choice
    .put(SegmentChoiceArrangementPick.class, SEGMENT_CHOICE_ARRANGEMENT_PICK) // after segment arrangement
    .put(PlatformMessage.class, PLATFORM_MESSAGE)
    .build();
  private static final Logger log = LoggerFactory.getLogger(DAORecord.class);
  private static Collection<String> nullValueClasses = ImmutableList.of("Null", "JsonNull");

  /**
   set all values from an entity onto a record

   @param target to set values on
   @param source to get value from
   @param <R>    type of record
   @param <E>    type of entity
   @throws CoreException on failure
   */
  public static <R extends Record, E extends Entity> void setAll(R target, E source) throws CoreException {

    // start with all of the entity resource attributes and their values
    Map<String, Object> attributes = source.getResourceAttributes();

    // add id for each belongs-to relationship
    for (Class cls : source.getResourceBelongsTo()) {
      String name = Text.toIdAttribute(cls);
      source.get(name).ifPresent(value -> attributes.put(name, value));
    }

    // set each attribute value
    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      String name = entry.getKey();
      Object value = entry.getValue();
      try {
        DAORecord.set(target, name, value);
      } catch (CoreException e) {
        log.error(String.format("Unable to set record %s attribute %s to value %s", target, name, value), e);
      }
    }


  }

  /**
   Get a value of a target object via attribute name

   @param attributeName of attribute to get
   @return value
   @throws CoreException on failure to get
   */
  public static <R extends Record> Optional<Object> get(R target, String attributeName) throws CoreException {
    String getterName = Text.toGetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return Entity.get(target, method);

        } catch (InvocationTargetException e) {
          throw new CoreException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(target), getterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new CoreException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(target), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   Set a value using an attribute name

   @param attributeName of attribute for which to find setter method
   @param value         to set
   */
  public static <R extends Record> void set(R target, String attributeName, Object value) throws CoreException {
    if (Objects.isNull(value)) return;

    String setterName = Text.toSetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          if (nullValueClasses.contains(value.getClass().getSimpleName()))
            setNull(target, method);
          else
            Entity.set(target, method, value);
          return;

        } catch (InvocationTargetException e) {
          throw new CoreException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new CoreException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));

        } catch (NoSuchMethodException e) {
          throw new CoreException(String.format("No such method %s.%s(), reason: %s", Text.getSimpleName(target), setterName, e.getMessage()));
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
  private static <R extends Record> void setNull(R target, Method setter) throws InvocationTargetException, IllegalAccessException, CoreException {
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
        throw new CoreException(String.format("Don't know how to set null value via %s", setter));
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
   @throws CoreException on failure
   */
  public static <R extends TableRecord<?>, N extends Entity> Collection<R> recordsFrom(DSLContext db, Table table, Collection<N> entities) throws CoreException {
    Collection<R> records = Lists.newArrayList();
    for (N e : entities) {
      R record = (R) db.newRecord(table);
      if (Objects.nonNull(e.getId()))
        set(record, "id", e.getId());
      setAll(record, e);
      records.add(record);
    }
    return records;
  }

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param modelClass instance of a single target entity
   @param records    to source values of
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  public static <N extends Entity, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws CoreException {
    Collection<N> models = Lists.newArrayList();
    for (R record : records) models.add(modelFrom(modelClass, record));
    return models;
  }

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param modelClass to whose setters the values will be written
   @param record     to source field-values of
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  public static <N extends Entity, R extends Record> N modelFrom(Class<N> modelClass, R record) throws CoreException {
    if (Objects.isNull(modelClass))
      throw new CoreException("Will not transmogrify null modelClass");

    // new instance of model
    N model;
    try {
      model = modelClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new CoreException(String.format("Could not get a new instance create class %s because %s", modelClass, e));
    }

    // set all values
    modelSetTransmogrified(record, model);

    return model;
  }

  /**
   Set all fields of an Entity using values transmogrified of a jOOQ Record

   @param record to transmogrify values of
   @param model  to set fields of
   @throws CoreException on failure to set transmogrified values
   */
  public static <N extends Entity, R extends Record> void modelSetTransmogrified(R record, N model) throws CoreException {
    if (Objects.isNull(record))
      throw new CoreException("Cannot transmogrify; record does not exist");

    Map<String, Object> fieldValues = record.intoMap();
    for (Map.Entry<String, Object> field : fieldValues.entrySet())
      if (DAOImpl.isNonNull(field.getValue())) try {
        String attributeName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, field.getKey());
        model.set(attributeName, field.getValue());
      } catch (Exception e) {
        log.error("Could not transmogrify key:{} val:{} because {}", field.getKey(), field.getValue(), e);
        throw new CoreException(String.format("Could not transmogrify key:%s val:%s because %s", field.getKey(), field.getValue(), e));
      }
  }

  /**
   Get a table record for an entity

   @param <N>    type of entity
   @param db     database context
   @param entity to get table record for
   @return table record
   @throws CoreException on failure
   */
  public static <N extends Entity> UpdatableRecord recordFor(DSLContext db, N entity) throws CoreException {
    Table table = tablesInSchemaConstructionOrder.get(entity.getClass());
    UpdatableRecord record = (UpdatableRecord) db.newRecord(table);

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
  public static <N extends Entity> N insert(DSLContext db, N entity) throws CoreException {
    UpdatableRecord record = recordFor(db, entity);
    record.store();
    DAORecord.get(record, "id").ifPresent(id ->
      entity.setId(UUID.fromString(String.valueOf(id))));
    return entity;
  }

  /**
   ids of a result set

   @param result set
   @return ids
   */
  public static Collection<UUID> idsFrom(Result<Record1<UUID>> result) {
    return result.map(Record1::value1);
  }

  /**
   Get DSL context

   @param connection SQL connection
   @return DSL context
   */
  public static DSLContext DSL(Connection connection) {
    return DSL.using(connection, SQLDialect.POSTGRES, DAOImpl.getSettings());
  }
}
