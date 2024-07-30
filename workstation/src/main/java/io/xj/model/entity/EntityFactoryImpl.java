// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.entity;

import io.xj.model.HubContent;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.ProgramState;
import io.xj.model.json.JsonProvider;
import io.xj.model.pojos.Template;
import io.xj.model.pojos.TemplateBinding;
import io.xj.model.util.ValueUtils;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 Implementation of Entity Factory
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class EntityFactoryImpl implements EntityFactory {
  static final Logger LOG = LoggerFactory.getLogger(EntityFactoryImpl.class);
  static final List<String> IGNORE_ATTRIBUTES = List.of("id", "class");
  final JsonProvider jsonProvider;
  Map<String, EntitySchema> schema = new ConcurrentHashMap<>();

  public EntityFactoryImpl(
    JsonProvider jsonProvider
  ) {
    this.jsonProvider = jsonProvider;
  }

  @Override
  public EntitySchema register(String type) {
    String key = EntityUtils.toType(type);
    if (schema.containsKey(key)) return schema.get(key);
    EntitySchema entitySchema = EntitySchema.of(type);
    schema.put(key, entitySchema);
    return entitySchema;
  }

  @Override
  public EntitySchema register(Class<?> typeClass) {
    String key = EntityUtils.toType(typeClass);
    if (schema.containsKey(key)) return schema.get(key);
    EntitySchema entitySchema = EntitySchema.of(typeClass);
    schema.put(key, entitySchema);
    return entitySchema;
  }

  @Override
  public <N> N getInstance(String type) throws EntityException {
    String key = EntityUtils.toType(type);
    ensureSchemaExists("get instance", key);
    @SuppressWarnings("unchecked") Supplier<N> creator = (Supplier<N>) schema.get(key).getCreator();
    if (Objects.isNull(creator))
      throw new EntityException(String.format("Failed to locate instance provider for %s", type));
    return creator.get();
  }

  @Override
  public <N> N getInstance(Class<N> type) throws EntityException {
    return getInstance(EntityUtils.toType(type));
  }

  @Override
  public Set<String> getBelongsTo(String type) throws EntityException {
    String key = EntityUtils.toType(type);
    ensureSchemaExists("get belongs-to type", key);
    return schema.get(key).getBelongsTo();
  }

  @Override
  public Set<String> getHasMany(String type) throws EntityException {
    String key = EntityUtils.toType(type);
    ensureSchemaExists("get has-many type", key);
    return schema.get(key).getHasMany();
  }

  @Override
  public Set<String> getAttributes(String type) throws EntityException {
    String key = EntityUtils.toType(type);
    ensureSchemaExists("get attribute", key);
    return schema.get(key).getAttributes();
  }

  @Override
  public <N> Map<String, Object> getResourceAttributes(N target) {
    Map<String, Object> attributes = new HashMap<>();
    //noinspection unchecked
    ReflectionUtils.getAllMethods(target.getClass(),
      ReflectionUtils.withModifier(Modifier.PUBLIC),
      ReflectionUtils.withPrefix("get"),
      ReflectionUtils.withParametersCount(0)).forEach(method -> {
      try {
        String attributeName = EntityUtils.toAttributeName(method);
        if (!IGNORE_ATTRIBUTES.contains(attributeName))
          EntityUtils.get(target, method).ifPresentOrElse(value -> attributes.put(attributeName, value),
            () -> attributes.put(attributeName, null));
      } catch (Exception e) {
        LOG.warn("Failed to transmogrify value create method {} create entity {}", method, target, e);
      }
    });
    return attributes;
  }

  @Override
  public <N> void setAllAttributes(N source, N target) {
    getResourceAttributes(source).forEach((Object name, Object attribute) -> {
      try {
        EntityUtils.set(target, String.valueOf(name), attribute);
      } catch (Exception e) {
        LOG.error("Failed to set {}", attribute, e);
      }
    });
  }

  @Override
  public <N> void setAllEmptyAttributes(N source, N target) {
    getResourceAttributes(source).forEach((Object name, Object value) -> {
      try {
        var tgtVal = EntityUtils.get(target, String.valueOf(name));
        if (tgtVal.isEmpty() || ValueUtils.isEmpty(tgtVal.get()))
          EntityUtils.set(target, String.valueOf(name), value);
      } catch (Exception e) {
        LOG.error("Failed to set {}", value, e);
      }
    });
  }

  @Override
  public <N> N duplicate(N from) throws EntityException {
    var duplicate = copy(from);
    EntityUtils.setId(duplicate, UUID.randomUUID());
    return duplicate;
  }

  @Override
  public <N> Map<UUID, N> duplicateAll(Collection<N> entities) throws EntityException {
    try {
      Map<UUID, N> duplicates = new HashMap<>();
      for (N entity : entities) duplicates.put(EntityUtils.getId(entity), duplicate(entity));
      return duplicates;
    } catch (Exception e) {
      throw new EntityException(e);
    }
  }

  @Override
  public <N> Map<UUID, N> duplicateAll(Collection<N> entities, Collection<?> newRelationships) throws EntityException {
    try {
      Map<UUID, N> duplicates = new HashMap<>();
      for (N entity : entities) {
        var duplicate = duplicate(entity);
        for (Object newRelationship : newRelationships)
          EntityUtils.set(duplicate, EntityUtils.toIdAttribute(newRelationship.getClass().getSimpleName()), EntityUtils.getId(newRelationship));
        duplicates.put(EntityUtils.getId(entity), duplicate);
      }
      return duplicates;
    } catch (Exception e) {
      throw new EntityException(e);
    }
  }

  @Override
  public <N> N copy(N from) throws EntityException {
    try {
      String className = from.getClass().getSimpleName();
      Object builder = getInstance(className);
      EntityUtils.setId(builder, EntityUtils.getId(from));
      setAllAttributes(from, builder);
      for (String belongsTo : getBelongsTo(className)) {
        Optional<UUID> belongsToId = EntityUtils.getBelongsToId(from, belongsTo);
        if (belongsToId.isPresent())
          EntityUtils.set(builder, EntityUtils.toIdAttribute(belongsTo), belongsToId.get());
      }
      //noinspection unchecked
      return (N) builder;
    } catch (Exception e) {
      throw new EntityException(e);
    }
  }

  @Override
  public <N> Collection<N> copyAll(Collection<N> entities) throws EntityException {
    try {
      Collection<N> copies = new HashSet<>();
      for (N entity : entities) copies.add(copy(entity));
      return copies;
    } catch (Exception e) {
      throw new EntityException(e);
    }
  }

  @Override
  public <N> N deserialize(Class<N> valueType, String json) throws EntityException {
    try {
      return jsonProvider.getMapper().readValue(String.valueOf(json), valueType);
    } catch (Exception e) {
      throw new EntityException("Failed to deserialize JSON", e);
    }
  }

  @Override
  public String serialize(Object obj) throws EntityException {
    try {
      return jsonProvider.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (Exception e) {
      throw new EntityException("Failed to serialize JSON", e);
    }
  }

  @Override
  public HubContent forTemplate(HubContent original, Template template) {
    HubContent content = new HubContent();

    // Add Template
    putCopy(content, template);

    // For each template binding, add the Library, Program, or Instrument
    for (TemplateBinding templateBinding : original.getBindingsOfTemplate(template.getId())) {
      putCopy(content, templateBinding);
      switch (templateBinding.getType()) {
        case Library -> putCopy(content, original.getLibrary(templateBinding.getTargetId()).orElseThrow());
        case Program -> putCopy(content, original.getProgram(templateBinding.getTargetId()).orElseThrow());
        case Instrument -> putCopy(content, original.getInstrument(templateBinding.getTargetId()).orElseThrow());
      }
    }

    // For each library, add the Programs that are in a published state
    content.getLibraries().stream()
      .flatMap(library -> original.getProgramsOfLibrary(library).stream())
      .filter(program -> program.getState().equals(ProgramState.Published))
      .forEach((entity) -> putCopy(content, entity));

    // For each library, add the Instruments that are in a published state
    content.getLibraries().stream()
      .flatMap(library -> original.getInstrumentsOfLibrary(library).stream())
      .filter(instrument -> instrument.getState().equals(InstrumentState.Published))
      .forEach((entity) -> putCopy(content, entity));

    // Add entities of Programs
    content.getPrograms().forEach(program -> {
      original.getMemesOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getVoicesOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getTracksOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequencesOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequenceBindingsOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequenceBindingMemesOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequenceChordsOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequenceChordVoicingsOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequencePatternsOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
      original.getSequencePatternEventsOfProgram(program.getId()).forEach((entity) -> putCopy(content, entity));
    });

    // Add entities of Instruments
    content.getInstruments().forEach(instrument -> {
      original.getMemesOfInstrument(instrument.getId()).forEach((entity) -> putCopy(content, entity));
      original.getAudiosOfInstrument(instrument.getId()).forEach((entity) -> putCopy(content, entity));
    });

    return content;
  }

  /**
   Put a duplicate of the given entity into the content

   @param content to put the duplicate into
   @param entity  to duplicate
   */
  private void putCopy(HubContent content, Object entity) {
    try {
      content.put(copy(entity));
    } catch (EntityException e) {
      LOG.error("Failed to duplicate entity", e);
    }
  }

  /**
   Ensure the given type exists in the inner schema, else add it

   @param message on failure
   @param type    to ensure existence of
   */
  void ensureSchemaExists(Object message, String type) throws EntityException {
    if (!schema.containsKey(type))
      throw new EntityException(String.format("Cannot %s unknown type: %s", message, type));
  }

}
