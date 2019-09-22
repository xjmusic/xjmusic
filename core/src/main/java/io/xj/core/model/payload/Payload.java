//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.payload;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import io.xj.core.config.Config;
import io.xj.core.model.entity.ResourceEntity;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.payload.deserializer.PayloadDeserializer;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 [#167276586] JSON API facilitates complex transactions
 */
@JsonDeserialize(using = PayloadDeserializer.class)
public class Payload {
  public static final String KEY_DATA = "data";
  public static final String KEY_INCLUDED = "included";
  public static final String KEY_LINKS = "links";
  public static final String KEY_ERRORS = "errors";
  public static final String KEY_SELF = "self";
  private final Map<String, String> links = Maps.newHashMap();
  private final Collection<PayloadObject> dataMany = Lists.newArrayList();
  private final Collection<PayloadError> errors = Lists.newArrayList();
  private final Collection<PayloadObject> included = Lists.newArrayList();
  @Nullable
  private PayloadObject dataOne;

  private PayloadDataType dataType = PayloadDataType.Ambiguous;

  /**
   Create a payload that is only a reference, containing only one primary object with type and id

   @param resourceType to reference
   @param resourceId   to reference
   @return Payload referencing specified resource type and id
   */
  public static Payload referenceTo(String resourceType, String resourceId) {
    return new Payload().setDataOne(new PayloadObject().setType(resourceType).setId(resourceId));
  }

  /**
   Add an object to has-many data

   @param resourceObject to add
   @return this Payload (for chaining methods)
   */
  public <N extends PayloadObject> Payload addData(N resourceObject) {
    dataMany.add(resourceObject);
    dataType = PayloadDataType.HasMany;
    return this;
  }

  /**
   Add a resource to the error objects@param payloadError which will be added to error objects

   @return this Payload (for chaining methods)
   */
  public <N extends PayloadError> Payload addError(N payloadError) {
    errors.add(payloadError);
    return this;
  }

  /**
   Add errors from entity

   @param entity to add errors from
   @param <N>    type of resource
   @return this payload (for chaining methods)
   */
  public <N extends ResourceEntity> Payload addErrorsOf(N entity) {
    entity.getErrors().forEach(exception -> addError(PayloadError.of(exception)));
    return this;
  }

  /**
   Add a resource to the included objects@param payloadObject which will be added to included objects

   @return this Payload (for chaining methods)
   */
  public Payload addIncluded(PayloadObject payloadObject) {
    included.add(payloadObject);
    return this;
  }

  /**
   Clear all contents

   @return this DataContainer (for chaining methods)
   */
  public Payload clear() {
    dataOne = null;
    dataMany.clear();
    included.clear();
    dataType = PayloadDataType.Ambiguous;
    return this;
  }

  /**
   Get has-many data

   @return collection of many data resource objects
   */
  public Collection<PayloadObject> getDataMany() {
    return dataMany;
  }

  /**
   Get has-one data

   @return one data resource object
   */
  public Optional<PayloadObject> getDataOne() {
    if (!PayloadDataType.HasOne.equals(dataType)) return Optional.empty();
    if (Objects.isNull(dataOne)) return Optional.empty();
    return Optional.of(dataOne);
  }

  /**
   Get container type, ambiguous, has-one, or has-many

   @return container type
   */
  public PayloadDataType getDataType() {
    return dataType;
  }

  /**
   Get errors

   @return errors
   */
  public Collection<PayloadError> getErrors() {
    return errors;
  }

  /**
   Get included resource objects

   @return included resource objects
   */
  public Collection<PayloadObject> getIncluded() {
    return included;
  }

  /**
   Get included resource objects matching a specified type

   @return included resource objects matching specified type
   */
  public Collection<PayloadObject> getIncludedOfType(String resourceType) {
    return included.stream()
      .filter(obj -> Objects.equals(resourceType, obj.getType()))
      .collect(Collectors.toList());
  }

  /**
   Get Links

   @return links
   */
  public Map<String, String> getLinks() {
    return links;
  }

  /**
   Get URI link to self

   @return URI of self
   */
  public URI getSelfURI() {
    if (links.containsKey(KEY_SELF))
      return URI.create(links.get(KEY_SELF));
    return Config.getApiURI("");
  }

  /**
   Add all sub-entities of given entity to included resource objects
   + cross-references all entities and adds hasMany references to payloads

   @param resources to be added
   @return this Payload (for chaining methods)
   */
  <N extends ResourceEntity> Payload addIncluded(Collection<N> resources) {
    resources.stream().map(resource -> resource.toPayloadObject(resources)).forEach(this::addIncluded);
    return this;
  }

  /**
   Is data container empty?

   @return true is empty
   */
  public boolean isEmpty() {
    return Objects.isNull(dataOne) &&
      dataMany.isEmpty() &&
      included.isEmpty() &&
      links.isEmpty();
  }

  /**
   Set has-many data

   @param payloadObjects to set
   @return this Payload (for chaining methods)
   */
  public <N extends PayloadObject> Payload setDataMany(Collection<N> payloadObjects) {
    clear();
    payloadObjects.forEach(this::addData);
    return this;
  }

  /**
   Set has-one data

   @param payloadObject to set
   @return this Payload (for chaining methods)
   */
  public <N extends PayloadObject> Payload setDataOne(N payloadObject) {
    clear();
    dataOne = payloadObject;
    dataType = PayloadDataType.HasOne;
    return this;
  }

  /**
   Set has-many data
   <p>
   NOTE we do NOT addIncluded(subEntities)
   + This is key to NOT sending overwhelmingly large resource index payloads
   + FUTURE maybe include partial filter of sub-entities

   @param entities           to set
   @param includeSubEntities if true, will include all sub-entities
   @return this Payload (for chaining methods)
   */
  public <N extends ResourceEntity> Payload setDataEntities(Collection<N> entities, boolean includeSubEntities) {
    clear();
    dataType = PayloadDataType.HasMany;
    entities.forEach(entity -> {
      Collection<SubEntity> subEntities = entity.getAllSubEntities();
      addData(entity.toPayloadObject(subEntities));
      addErrorsOf(entity);
      if (includeSubEntities) addIncluded(subEntities);
    });
    return this;
  }

  /**
   Set has-one data

   @param entity to set
   @return this Payload (for chaining methods)
   */
  public <N extends ResourceEntity> Payload setDataEntity(N entity) {
    clear();
    Collection<SubEntity> subEntities = entity.getAllSubEntities();
    setDataOne(entity.toPayloadObject(subEntities));
    addIncluded(subEntities);
    addErrorsOf(entity);
    return this;
  }

  /**
   Set Data as reference to one type and id

   @param type to reference
   @param id   to reference
   @return this Payload (for chaining methods)
   */
  public Payload setDataReference(String type, String id) {
    return setDataOne(new PayloadObject().setType(type).setId(id));
  }

  /**
   Set Data as references to many type and id

   @param resources to reference
   @return this payload (for chaining methods)
   */
  public <R extends ResourceEntity> Payload setDataReferences(Collection<R> resources) {
    setDataType(PayloadDataType.HasMany);
    resources.stream().map(ResourceEntity::toPayloadReferenceObject).forEach(this::addData);
    return this;
  }

  /**
   Set data type

   @param dataType to set
   @return this Payload (for chaining methods)
   */
  public Payload setDataType(PayloadDataType dataType) {
    this.dataType = dataType;
    return this;
  }

  /**
   Set all included

   @param included to set
   @return this Payload (for chaining methods)
   */
  public Payload setIncluded(Collection<PayloadObject> included) {
    this.included.clear();
    this.included.addAll(included);
    return this;
  }

  /**
   Set all links

   @param links to set
   @return this Payload (for chaining methods)
   */
  public Payload setLinks(Map<String, String> links) {
    this.links.clear();
    this.links.putAll(links);
    return this;
  }

  /**
   Set URI link to self

   @param uri link to set
   @return this Payload (for chaining methods)
   */
  public Payload setSelfURI(URI uri) {
    links.put(KEY_SELF, uri.toString());
    return this;
  }
}
