// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Implementation of Payload Factory
 <p>
 Created by Charney Kaye on 2020/03/09
 */
@Singleton
public class PayloadFactoryImpl implements PayloadFactory {
  private static final Logger log = LoggerFactory.getLogger(PayloadFactoryImpl.class);
  private final ObjectMapper mapper = new ObjectMapper();
  Map<String, PayloadEntitySchema> schema = Maps.newConcurrentMap();

  @Inject
  PayloadFactoryImpl() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    mapper.registerModule(module);
  }

  @Override
  public PayloadEntitySchema register(String type) {
    String key = PayloadKey.toResourceType(type);
    if (schema.containsKey(key)) return schema.get(key);
    PayloadEntitySchema entitySchema = PayloadEntitySchema.of(type);
    schema.put(key, entitySchema);
    return entitySchema;
  }

  @Override
  public PayloadEntitySchema register(Class<?> typeClass) {
    String key = PayloadKey.toResourceType(typeClass);
    if (schema.containsKey(key)) return schema.get(key);
    PayloadEntitySchema entitySchema = PayloadEntitySchema.of(typeClass);
    schema.put(key, entitySchema);
    return entitySchema;
  }

  @Override
  public Object getInstance(String type) throws RestApiException {
    String key = PayloadKey.toResourceType(type);
    ensureSchemaExists("get instance", key);
    return schema.get(key).getCreator().get();
  }

  @Override
  public Set<String> getBelongsTo(String type) throws RestApiException {
    String key = PayloadKey.toResourceType(type);
    ensureSchemaExists("get belongs-to type", key);
    return schema.get(key).getBelongsTo();
  }

  @Override
  public Set<String> getHasMany(String type) throws RestApiException {
    String key = PayloadKey.toResourceType(type);
    ensureSchemaExists("get has-many type", key);
    return schema.get(key).getHasMany();
  }

  @Override
  public Set<String> getAttributes(String type) throws RestApiException {
    String key = PayloadKey.toResourceType(type);
    ensureSchemaExists("get attribute", key);
    return schema.get(key).getAttributes();
  }

  @Override
  public <N> N consume(N target, Payload payload) throws RestApiException {
    consume(target, extractPrimaryObject(payload, PayloadKey.toResourceType(target)));
    return target;
  }

  @Override
  public <N> N consume(N target, PayloadObject payloadObject) throws RestApiException {
    String targetType = PayloadKey.toResourceType(target);

    if (!Objects.equals(payloadObject.getType(), targetType))
      throw new RestApiException(String.format("Cannot set single %s-type entity create %s-type payload object!", targetType, payloadObject.getType()));

    for (Map.Entry<String, Object> entry : payloadObject.getAttributes().entrySet())
      PayloadEntity.set(target, entry.getKey(), entry.getValue());

    // consume all belongs-to relationships
    getBelongsTo(targetType).forEach(key -> {
      Optional<PayloadObject> obj = payloadObject.getRelationshipDataOne(key);
      obj.ifPresent(object -> {
        try {
          PayloadEntity.set(target, PayloadKey.toIdAttribute(key), object.getId());
        } catch (RestApiException e) {
          log.error("Failed to consume belongs-to {} relationship", key, e);
        }
      });
    });

    // consume if if set
    if (Objects.nonNull(payloadObject.getId())) try {
      PayloadEntity.set(target, "id", UUID.fromString(payloadObject.getId()));
    } catch (IllegalArgumentException exception) {
      log.error("Failed to consume id from payload object.getId(): {}", payloadObject.getId());
    }


    return target;
  }

  @Override
  public <N> Map<String, Object> getResourceAttributes(N target) throws RestApiException {
    String targetType = PayloadKey.toResourceType(target);
    Map<String, Object> attributes = Maps.newHashMap();
    Set<String> resourceAttributeNames = getAttributes(targetType);
    //noinspection unchecked
    ReflectionUtils.getAllMethods(target.getClass(),
      ReflectionUtils.withModifier(Modifier.PUBLIC),
      ReflectionUtils.withPrefix("get"),
      ReflectionUtils.withParametersCount(0)).forEach(method -> {
      try {
        String attributeName = PayloadKey.toAttributeName(method);
        if (resourceAttributeNames.contains(attributeName)) {
          PayloadEntity.get(target, method).ifPresentOrElse(value -> attributes.put(attributeName, value),
            () -> attributes.put(attributeName, null));
        }
      } catch (Exception e) {
        log.warn("Failed to transmogrify value create method {} create entity {}", method, target, e);
      }
    });
    return attributes;
  }

  @Override
  public <N> PayloadObject toPayloadObject(N target) throws RestApiException {
    return toPayloadObject(target, ImmutableList.of());
  }

  @Override
  public <N> PayloadObject toPayloadObject(N target, Collection<N> childResources) throws RestApiException {
    String targetType = PayloadKey.toResourceType(target);
    PayloadObject obj = toPayloadReferenceObject(target);
    obj.setAttributes(getResourceAttributes(target));

    // add belongs-to
    getBelongsTo(targetType).forEach(key -> {
      try {
        Optional<Object> value = PayloadEntity.get(target, PayloadKey.toIdAttribute(key));
        value.ifPresent(id -> obj.add(PayloadKey.toResourceBelongsTo(key),
          new Payload().setDataReference(PayloadKey.toResourceType(key), String.valueOf(id))));
      } catch (RestApiException e) {
        log.error("Failed to add belongs-to {} relationship", key, e);
      }
    });

    // add has-many
    Map<String, Collection<N>> hasMany = Maps.newConcurrentMap();
    childResources.forEach(resource -> {
      String type = PayloadKey.toResourceType(resource);
      if (!hasMany.containsKey(type)) hasMany.put(type, Lists.newArrayList());
      hasMany.get(type).add(resource);
    });
    for (String key : getHasMany(targetType)) {
      String type = PayloadKey.toResourceType(key);
      if (hasMany.containsKey(type) && !hasMany.get(type).isEmpty())
        obj.add(PayloadKey.toResourceHasMany(key),
          setDataReferences(newPayload(), hasMany.containsKey(type) ?
            hasMany.get(type).stream().filter(r -> belongsTo(r, target)).collect(Collectors.toList()) :
            ImmutableList.of()));
    }

    return obj;
  }

  @Override
  public Payload newPayload() {
    return new Payload();
  }

  @Override
  public PayloadObject newPayloadObject() {
    return new PayloadObject();
  }

  @Override
  public PayloadError newPayloadError() {
    return new PayloadError();
  }

  @Override
  public <N> PayloadObject toPayloadReferenceObject(N target) throws RestApiException {
    return new PayloadObject()
      .setId(getResourceId(target))
      .setType(PayloadKey.toResourceType(target));
  }

  @Override
  public <N> void setAllAttributes(N source, N target) throws RestApiException {
    getResourceAttributes(source).forEach((Object name, Object attribute) -> {
      try {
        PayloadEntity.set(target, String.valueOf(name), attribute);
      } catch (RestApiException e) {
        log.error("Failed to set {}", attribute, e);
      }
    });
  }

  @Override
  public <N> Payload addIncluded(Payload payload, Collection<N> resources) throws RestApiException {
    for (N resource : resources) {
      PayloadObject payloadObject = toPayloadObject(resource, resources);
      addIncluded(payload, payloadObject);
    }
    return payload;
  }

  @Override
  public Payload addIncluded(Payload payload, PayloadObject payloadObject) throws RestApiException {
    switch (payload.getDataType()) {
      case HasMany:
        for (PayloadObject obj : payload.getDataMany())
          addIfRelated(obj, payloadObject);
        break;
      case HasOne:
        if (payload.getDataOne().isPresent())
          addIfRelated(payload.getDataOne().get(), payloadObject);
        break;
      case Ambiguous:
        break;
    }
    payload.getIncluded().add(payloadObject);
    return payload;
  }

  @Override
  public void addIfRelated(PayloadObject obj, PayloadObject rel) throws RestApiException {
    String hasMany = PayloadKey.toResourceHasManyFromType(rel.getType());
    String belongsTo = PayloadKey.toResourceBelongsToFromType(obj.getType());
    if (rel.getRelationships().containsKey(belongsTo)) {
      Optional<PayloadObject> search = rel.getRelationships().get(belongsTo).getDataOne();
      if (search.isPresent()
        && search.get().getType().equals(obj.getType())
        && search.get().getId().equals(obj.getId())) {
        add(obj, hasMany, rel.toMinimal());
      }
    }
  }


  @Override
  public <N> Payload setDataEntities(Payload payload, Collection<N> entities) throws RestApiException {
    payload.clear();
    payload.setDataType(PayloadDataType.HasMany);
    for (N entity : entities)
      payload.addData(toPayloadObject(entity));
    return payload;
  }

  @Override
  public <N> Payload setDataEntity(Payload payload, N entity) throws RestApiException {
    payload.clear();
    payload.setDataOne(toPayloadObject(entity));
    return payload;
  }

  @Override
  public <R> Payload setDataReferences(Payload payload, Collection<R> resources) throws RestApiException {
    payload.setDataType(PayloadDataType.HasMany);
    for (R resource : resources) {
      PayloadObject payloadObject = toPayloadReferenceObject(resource);
      payload.addData(payloadObject);
    }
    return payload;
  }

  @Override
  public PayloadObject add(PayloadObject obj, String relationshipName, PayloadObject payloadObject) throws RestApiException {
    if (!obj.getRelationships().containsKey(relationshipName))
      obj.getRelationships().put(relationshipName, setDataReferences(newPayload(), ImmutableList.of()));
    obj.getRelationships().get(relationshipName).addToDataMany(payloadObject);
    return obj;
  }

  @Override
  public Payload deserialize(String payloadAsJSON) throws RestApiException {
    return deserialize(payloadAsJSON, Payload.class);
  }

  @Override
  public <N> N deserialize(String json, Class<N> valueType) throws RestApiException {
    try {
      return mapper.readValue(String.valueOf(json), valueType);
    } catch (JsonProcessingException e) {
      throw new RestApiException("Failed to deserialize JSON", e);
    }
  }

  @Override
  public String serialize(Object obj) throws RestApiException {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new RestApiException("Failed to serialize JSON", e);
    }
  }

  /**
   Require a payload to have one resource object of a specified type

   @param payload      to inspect
   @param resourceType to require a primary object of
   @throws RestApiException if there exists NO primary object of the specified type
   */
  private PayloadObject extractPrimaryObject(Payload payload, String resourceType) throws RestApiException {
    Optional<PayloadObject> obj = payload.getDataOne();
    if (obj.isEmpty())
      throw new RestApiException("Cannot deserialize single entity create payload without singular data!");
    if (!Objects.equals(resourceType, obj.get().getType()))
      throw new RestApiException(String.format("Cannot deserialize single %s-type entity create %s-type payload!", resourceType, obj.get().getType()));
    return obj.get();
  }

  /**
   Whether target resource belongs to the specified resource

   @param target   to test for parenthood
   @param resource to test whether target belongs to
   @return true if target belongs to the specified resource
   */
  private <N> boolean belongsTo(N target, N resource) {
    try {
      Optional<Object> id = PayloadEntity.get(target, PayloadKey.toIdAttribute(resource));
      return id.isPresent() && id.get().equals(getResourceId(resource));
    } catch (RestApiException e) {
      return false;
    }
  }

  /**
   get Entity ID

   @return Entity Id
   */
  private <N> String getResourceId(N target) throws RestApiException {
    Optional<Object> id = PayloadEntity.get(target, PayloadObject.KEY_ID);
    if (id.isEmpty()) throw new RestApiException("Has no id");
    return (id.get().toString());
  }

  /**
   Ensure the given type exists in the inner schema, else add it@param message

   @param type to ensure existence of
   */
  private void ensureSchemaExists(Object message, String type) throws RestApiException {
    if (!schema.containsKey(type))
      throw new RestApiException(String.format("Cannot %s unknown type: %s", message, type));
  }

}
