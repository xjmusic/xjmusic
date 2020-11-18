// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.MessageLite;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.InstantDeserializer;
import io.xj.lib.entity.InstantSerializer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Implementation of Payload Factory
 <p>
 Created by Charney Kaye on 2020/03/09
 */
@Singleton
public class PayloadFactoryImpl implements PayloadFactory {
  private final EntityFactory entityFactory;
  private final ObjectMapper mapper = new ObjectMapper();

  @Inject
  PayloadFactoryImpl(
    EntityFactory entityFactory
  ) {
    this.entityFactory = entityFactory;
    SimpleModule module = new SimpleModule();
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    mapper.registerModule(module);
  }

  @Override
  public <N extends MessageLite> N consume(N target, Payload payload) throws JsonApiException {
    return consume(target, extractPrimaryObject(payload, Entities.toType(target)));
  }

  @Override
  public <N extends MessageLite> N consume(N instance, PayloadObject payloadObject) throws JsonApiException {
    try {
      String targetType = Entities.toType(instance);
      if (!Objects.equals(payloadObject.getType(), targetType))
        throw new JsonApiException(String.format("Cannot set single %s-type entity create %s-type payload object!", targetType, payloadObject.getType()));
      MessageLite.Builder target = instance.toBuilder();

      for (Map.Entry<String, Object> entry : payloadObject.getAttributes().entrySet())
        Entities.set(target, entry.getKey(), entry.getValue());

      // consume all belongs-to relationships
      entityFactory.getBelongsTo(targetType).forEach(key -> {
        Optional<PayloadObject> obj = payloadObject.getRelationshipDataOne(key);
        obj.ifPresent(object -> {
          try {
            Entities.set(target, Entities.toIdAttribute(key), object.getId());
          } catch (EntityException ignored) {
            // n/a -- objects can be created with missing relationships
          }
        });
      });

      // consume if if set
      if (Objects.nonNull(payloadObject.getId())) try {
        Entities.set(target, "id", payloadObject.getId());
      } catch (IllegalArgumentException ignored) {
        // n/a -- objects can be created without ID
      }

      //noinspection unchecked
      return (N) target.build();
    } catch (EntityException e) {
      throw new JsonApiException(e);
    }
  }

  @Override
  public <N> PayloadObject toPayloadObject(N target) throws JsonApiException {
    return toPayloadObject(target, ImmutableList.of());
  }

  @Override
  public <N> Collection<PayloadObject> toPayloadObjects(Collection<N> targets) throws JsonApiException {
    List<PayloadObject> list = new ArrayList<>();
    for (N target : targets) list.add(toPayloadObject(target));
    return list;
  }

  @Override
  public <N> PayloadObject toPayloadObject(N target, Collection<N> childResources) throws JsonApiException {
    try {
      String targetType = Entities.toType(target);
      PayloadObject obj = toPayloadReferenceObject(target);
      obj.setAttributes(entityFactory.getResourceAttributes(target));

      // add belongs-to
      for (String s : entityFactory.getBelongsTo(targetType)) {
        Optional<Object> value = Entities.get(target, Entities.toIdAttribute(s));
        value.ifPresent(id -> obj.add(Entities.toBelongsTo(s),
          new Payload().setDataReference(Entities.toType(s), String.valueOf(id))));
      }

      // add has-many
      Map<String, Collection<N>> hasMany = Maps.newConcurrentMap();
      childResources.forEach(resource -> {
        String type = Entities.toType(resource);
        if (!hasMany.containsKey(type)) hasMany.put(type, Lists.newArrayList());
        hasMany.get(type).add(resource);
      });
      for (String key : entityFactory.getHasMany(targetType)) {
        String type = Entities.toType(key);
        if (hasMany.containsKey(type) && !hasMany.get(type).isEmpty())
          obj.add(Entities.toHasMany(key),
            setDataReferences(newPayload(), hasMany.containsKey(type) ?
              hasMany.get(type).stream().filter(r -> belongsTo(r, target)).collect(Collectors.toList()) :
              ImmutableList.of()));
      }

      return obj;
    } catch (EntityException e) {
      throw new JsonApiException(e);
    }
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
  public <N> PayloadObject toPayloadReferenceObject(N target) throws JsonApiException {
    return new PayloadObject()
      .setId(getResourceId(target))
      .setType(Entities.toType(target));
  }

  @Override
  public <N> Payload addIncluded(Payload payload, Collection<N> resources) throws JsonApiException {
    for (N resource : resources) {
      PayloadObject payloadObject = toPayloadObject(resource, resources);
      addIncluded(payload, payloadObject);
    }
    return payload;
  }

  @Override
  public Payload addIncluded(Payload payload, PayloadObject payloadObject) throws JsonApiException {
    switch (payload.getDataType()) {
      case Many:
        for (PayloadObject obj : payload.getDataMany())
          addIfRelated(obj, payloadObject);
        break;
      case One:
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
  public void addIfRelated(PayloadObject obj, PayloadObject rel) throws JsonApiException {
    String hasMany = Entities.toHasManyFromType(rel.getType());
    String belongsTo = Entities.toBelongsToFromType(obj.getType());
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
  public <N> Payload setDataEntities(Payload payload, Collection<N> entities) throws JsonApiException {
    payload.clear();
    payload.setDataType(PayloadDataType.Many);
    for (N entity : entities)
      payload.addData(toPayloadObject(entity));
    return payload;
  }

  @Override
  public <N> Payload setDataEntity(Payload payload, N entity) throws JsonApiException {
    payload.clear();
    payload.setDataOne(toPayloadObject(entity));
    return payload;
  }

  @Override
  public <N> Payload setDataReferences(Payload payload, Collection<N> entities) throws JsonApiException {
    payload.setDataType(PayloadDataType.Many);
    for (N resource : entities) {
      PayloadObject payloadObject = toPayloadReferenceObject(resource);
      payload.addData(payloadObject);
    }
    return payload;
  }

  @Override
  public PayloadObject add(PayloadObject obj, String relationshipName, PayloadObject payloadObject) throws JsonApiException {
    if (!obj.getRelationships().containsKey(relationshipName))
      obj.getRelationships().put(relationshipName, setDataReferences(newPayload(), ImmutableList.of()));
    obj.getRelationships().get(relationshipName).addToDataMany(payloadObject);
    return obj;
  }

  @Override
  public Payload filter(Payload payload, Class<?> type, Predicate<? super PayloadObject> condition) throws JsonApiException {
    switch (payload.getDataType()) {

      case Many:
        Collection<PayloadObject> hasMany =
          payload.getDataMany().stream()
            .filter(condition)
            .collect(Collectors.toList());
        Collection<PayloadObject> included =
          payload.getIncluded().stream()
            .filter(obj -> obj.belongsTo(type, hasMany.stream()
              .filter(par -> par.isType(type))
              .map(PayloadObject::getId)
              .collect(Collectors.joining())))
            .collect(Collectors.toList());
        return newPayload()
          .setDataMany(hasMany)
          .setIncluded(included);

      case Ambiguous:
      case One:
      default:
        throw new JsonApiException("I only know how to filter data-many payloads!");
    }
  }

  @Override
  public Payload filterIncluded(Payload payload, Predicate<? super PayloadObject> condition) throws JsonApiException {
    switch (payload.getDataType()) {

      case One:
        return newPayload()
          .setDataOne(payload.getDataOne().orElseThrow(() ->
            new JsonApiException("cannot filter included entities of empty data-one payload!")))
          .setIncluded(payload.getIncluded().stream()
            .filter(condition)
            .collect(Collectors.toList()));

      case Many:
        return newPayload()
          .setDataMany(payload.getDataMany())
          .setIncluded(payload.getIncluded().stream()
            .filter(condition)
            .collect(Collectors.toList()));

      case Ambiguous:
      default:
        throw new JsonApiException("cannot filter the included entities of an ambiguous payload!");
    }
  }

  @Override
  public Payload sort(Payload payload, Class<?> type, Function<? super PayloadObject, Long> keyExtractor) throws JsonApiException {
    switch (payload.getDataType()) {

      case Many:
        Collection<PayloadObject> hasMany =
          payload.getDataMany().stream()
            .sorted(Comparator.comparing(keyExtractor))
            .collect(Collectors.toList());
        Collection<PayloadObject> included =
          payload.getIncluded().stream()
            .filter(obj -> obj.belongsTo(type, hasMany.stream()
              .filter(par -> par.isType(type))
              .map(PayloadObject::getId)
              .collect(Collectors.joining())))
            .collect(Collectors.toList());
        return newPayload()
          .setDataMany(hasMany)
          .setIncluded(included);

      case Ambiguous:
      case One:
      default:
        throw new JsonApiException("I only know how to filter data-many payloads!");
    }
  }

  @Override
  public Payload limit(Payload payload, Class<?> type, long limit) throws JsonApiException {
    switch (payload.getDataType()) {

      case Many:
        Collection<PayloadObject> hasMany =
          payload.getDataMany().stream()
            .limit(limit)
            .collect(Collectors.toList());
        Collection<PayloadObject> included =
          payload.getIncluded().stream()
            .filter(obj -> obj.belongsTo(type, hasMany.stream()
              .filter(par -> par.isType(type))
              .map(PayloadObject::getId)
              .collect(Collectors.joining())))
            .collect(Collectors.toList());
        return newPayload()
          .setDataMany(hasMany)
          .setIncluded(included);

      case Ambiguous:
      case One:
      default:
        throw new JsonApiException("can only filter data-many payloads!");
    }
  }

  @Override
  public Payload find(Payload payload, Class<?> type, Predicate<? super PayloadObject> condition) throws JsonApiException {
    switch (payload.getDataType()) {

      case Many:
        PayloadObject one =
          payload.getDataMany().stream()
            .filter(condition)
            .findFirst()
            .orElseThrow();
        Collection<PayloadObject> included =
          payload.getIncluded().stream()
            .filter(obj -> obj.belongsTo(type, ImmutableList.of(one.getId())))
            .collect(Collectors.toList());
        return newPayload()
          .setDataOne(one)
          .setIncluded(included);

      case Ambiguous:
      case One:
      default:
        throw new JsonApiException("can only find one from data-many payloads!");
    }
  }

  @Override
  public Payload deserialize(String payloadAsJSON) throws JsonApiException {
    return deserialize(Payload.class, payloadAsJSON);
  }

  @Override
  public <N> N deserialize(Class<N> valueType, String json) throws JsonApiException {
    try {
      return mapper.readValue(String.valueOf(json), valueType);
    } catch (JsonProcessingException e) {
      throw new JsonApiException("Failed to deserialize JSON", e);
    }
  }

  @Override
  public <N extends MessageLite> N toOne(PayloadObject payloadObject) throws JsonApiException {
    try {
      N defaultInstance = entityFactory.getInstance(payloadObject.getType());
      return consume(defaultInstance, payloadObject);
    } catch (EntityException e) {
      throw new JsonApiException(e);
    }
  }

  @Override
  public <N extends MessageLite> N toOne(Payload payload) throws JsonApiException {
    return toOne(payload.getDataOne().orElseThrow(() ->
      new JsonApiException("Can only create instance from data-one payload!")));
  }

  @Override
  public <N extends MessageLite> Collection<N> toMany(Payload payload) throws JsonApiException {
    if (!PayloadDataType.Many.equals(payload.getDataType()))
      throw new JsonApiException("Can only create instances from data-many payload!");
    Collection<N> instances = Lists.newArrayList();
    for (PayloadObject payloadObject : payload.getDataMany())
      instances.add(toOne(payloadObject));
    for (PayloadObject payloadObject : payload.getIncluded())
      instances.add(toOne(payloadObject));
    return instances;
  }

  @Override
  public <N extends MessageLite> Payload from(N entity) throws JsonApiException {
    return newPayload()
      .setDataOne(toPayloadObject(entity));
  }

  @Override
  public <N extends MessageLite> Payload from(N entity, Collection<N> included) throws JsonApiException {
    return newPayload()
      .setDataOne(toPayloadObject(entity))
      .setIncluded(toPayloadObjects(included));
  }

  @Override
  public <N extends MessageLite> Payload from(Collection<N> entities) throws JsonApiException {
    return newPayload()
      .setDataMany(toPayloadObjects(entities));
  }

  @Override
  public <N extends MessageLite> Payload from(Collection<N> entities, Collection<N> included) throws JsonApiException {
    return newPayload()
      .setDataMany(toPayloadObjects(entities))
      .setIncluded(toPayloadObjects(included));
  }

  @Override
  public String serialize(Object obj) throws JsonApiException {
    try {
      return mapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new JsonApiException("Failed to serialize JSON", e);
    }
  }

  /**
   Require a payload to have one resource object of a specified type

   @param payload      to inspect
   @param resourceType to require a primary object of
   @throws JsonApiException if there exists NO primary object of the specified type
   */
  private PayloadObject extractPrimaryObject(Payload payload, String resourceType) throws JsonApiException {
    Optional<PayloadObject> obj = payload.getDataOne();
    if (obj.isEmpty())
      throw new JsonApiException("Cannot deserialize single entity create payload without singular data!");
    if (!Objects.equals(resourceType, obj.get().getType()))
      throw new JsonApiException(String.format("Cannot deserialize single %s-type entity create %s-type payload!", resourceType, obj.get().getType()));
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
      Optional<Object> id = Entities.get(target, Entities.toIdAttribute(resource));
      return id.isPresent() && id.get().equals(getResourceId(resource));
    } catch (JsonApiException | EntityException e) {
      return false;
    }
  }

  /**
   get Entity ID

   @return Entity Id
   */
  private <N> String getResourceId(N target) throws JsonApiException {
    try {
      Optional<Object> id = Entities.get(target, PayloadObject.KEY_ID);
      if (id.isEmpty()) throw new JsonApiException("Has no id");
      return (id.get().toString());
    } catch (EntityException e) {
      throw new JsonApiException(e);
    }
  }
}
