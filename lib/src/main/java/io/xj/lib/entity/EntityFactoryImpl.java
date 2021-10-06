// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.json.JsonProviderImpl;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 Implementation of Entity Factory
 <p>
 Created by Charney Kaye on 2020/03/09
 */
@Singleton
public class EntityFactoryImpl implements EntityFactory {
  private static final Logger log = LoggerFactory.getLogger(EntityFactoryImpl.class);
  private static final String ATTR_ID = "id";
  private final JsonProviderImpl jsonProvider;
  Map<String, EntitySchema> schema = Maps.newConcurrentMap();

  @Inject
  EntityFactoryImpl(
    JsonProviderImpl jsonProvider
  ) {
    this.jsonProvider = jsonProvider;
  }

  @Override
  public EntitySchema register(String type) {
    String key = Entities.toType(type);
    if (schema.containsKey(key)) return schema.get(key);
    EntitySchema entitySchema = EntitySchema.of(type);
    schema.put(key, entitySchema);
    return entitySchema;
  }

  @Override
  public EntitySchema register(Class<?> typeClass) {
    String key = Entities.toType(typeClass);
    if (schema.containsKey(key)) return schema.get(key);
    EntitySchema entitySchema = EntitySchema.of(typeClass);
    schema.put(key, entitySchema);
    return entitySchema;
  }

  @Override
  public <N> N getInstance(String type) throws EntityException {
    String key = Entities.toType(type);
    ensureSchemaExists("get instance", key);
    @SuppressWarnings("unchecked") Supplier<N> creator = (Supplier<N>) schema.get(key).getCreator();
    if (Objects.isNull(creator))
      throw new EntityException(String.format("Failed to locate instance provider for %s", type));
    return creator.get();
  }

  @Override
  public <N> N getInstance(Class<N> type) throws EntityException {
    return getInstance(Entities.toType(type));
  }

  @Override
  public Set<String> getBelongsTo(String type) throws EntityException {
    String key = Entities.toType(type);
    ensureSchemaExists("get belongs-to type", key);
    return schema.get(key).getBelongsTo();
  }

  @Override
  public Set<String> getBelongsTo(Class<?> type) throws EntityException {
    return getBelongsTo(type.getSimpleName());
  }

  @Override
  public Set<String> getHasMany(String type) throws EntityException {
    String key = Entities.toType(type);
    ensureSchemaExists("get has-many type", key);
    return schema.get(key).getHasMany();
  }

  @Override
  public Set<String> getAttributes(String type) throws EntityException {
    String key = Entities.toType(type);
    ensureSchemaExists("get attribute", key);
    return schema.get(key).getAttributes();
  }

  @Override
  public <N> Map<String, Object> getResourceAttributes(N target) {
    Map<String, Object> attributes = Maps.newHashMap();
    //noinspection unchecked
    ReflectionUtils.getAllMethods(target.getClass(),
      ReflectionUtils.withModifier(Modifier.PUBLIC),
      ReflectionUtils.withPrefix("get"),
      ReflectionUtils.withParametersCount(0)).forEach(method -> {
      try {
        String attributeName = Entities.toAttributeName(method);
        if (!Objects.equals(ATTR_ID, attributeName))
          Entities.get(target, method).ifPresentOrElse(value -> attributes.put(attributeName, value),
            () -> attributes.put(attributeName, null));
      } catch (Exception e) {
        log.warn("Failed to transmogrify value create method {} create entity {}", method, target, e);
      }
    });
    return attributes;
  }

  @Override
  public <N> void setAllAttributes(N source, N target) {
    getResourceAttributes(source).forEach((Object name, Object attribute) -> {
      try {
        Entities.set(target, String.valueOf(name), attribute);
      } catch (EntityException e) {
        log.error("Failed to set {}", attribute, e);
      }
    });
  }

  @Override
  public boolean isKnownSchema(String entityName) {
    return schema.containsKey(Entities.toType(entityName));
  }

  @Override
  public boolean isKnownSchema(Class<?> entityClass) {
    return schema.containsKey(Entities.toType(entityClass));
  }

  @Override
  public boolean isKnownSchema(Object entity) {
    return schema.containsKey(Entities.toType(entity));
  }

  @Override
  public <N> N clone(N from) throws EntityException {
    String className = from.getClass().getSimpleName();
    Object builder = getInstance(className);
    Entities.setId(builder, Entities.getId(from));
    setAllAttributes(from, builder);
    for (String belongsTo : getBelongsTo(className)) {
      Optional<UUID> belongsToId = Entities.getBelongsToId(from, belongsTo);
      if (belongsToId.isPresent())
        Entities.set(builder, Entities.toIdAttribute(belongsTo), belongsToId.get());
    }
    //noinspection unchecked
    return (N) builder;
  }

  @Override
  public <N> Collection<N> cloneAll(Collection<N> entities) throws EntityException {
    Collection<N> clones = Lists.newArrayList();
    for (N entity : entities) clones.add(clone(entity));
    return clones;
  }

  @Override
  public <N> N deserialize(Class<N> valueType, String json) throws EntityException {
    try {
      return jsonProvider.getObjectMapper().readValue(String.valueOf(json), valueType);
    } catch (JsonProcessingException e) {
      throw new EntityException("Failed to deserialize JSON", e);
    }
  }

  @Override
  public String serialize(Object obj) throws EntityException {
    try {
      return jsonProvider.getObjectMapper().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new EntityException("Failed to serialize JSON", e);
    }
  }

  /**
   Ensure the given type exists in the inner schema, else add it

   @param message on failure
   @param type    to ensure existence of
   */
  private void ensureSchemaExists(Object message, String type) throws EntityException {
    if (!schema.containsKey(type))
      throw new EntityException(String.format("Cannot %s unknown type: %s", message, type));
  }

}
