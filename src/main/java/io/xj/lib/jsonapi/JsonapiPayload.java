// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 <p>
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
@JsonDeserialize(using = JsonapiPayloadDeserializer.class)
@JsonSerialize(using = JsonapiPayloadSerializer.class)
public class JsonapiPayload {
  public static final String KEY_DATA = "data";
  public static final String KEY_INCLUDED = "included";
  public static final String KEY_LINKS = "links";
  public static final String KEY_ERRORS = "errors";
  public static final String KEY_SELF = "self";
  private final Map<String, String> links = Maps.newHashMap();
  private final Collection<JsonapiPayloadObject> dataMany = Lists.newArrayList();
  private final Collection<PayloadError> errors = Lists.newArrayList();
  private final Collection<JsonapiPayloadObject> included = Lists.newArrayList();
  private PayloadDataType dataType = PayloadDataType.Ambiguous;

  @Nullable
  private JsonapiPayloadObject dataOne;


  /**
   Add an object to has-many data

   @param resourceObject to add
   @return this Payload (for chaining methods)
   */
  public <N extends JsonapiPayloadObject> JsonapiPayload addData(N resourceObject) {
    dataMany.add(resourceObject);
    dataType = PayloadDataType.Many;
    return this;
  }

  /**
   Add a resource to the error objects@param payloadError which will be added to error objects

   @return this Payload (for chaining methods)
   */
  public <N extends PayloadError> JsonapiPayload addError(N payloadError) {
    errors.add(payloadError);
    return this;
  }

  /**
   Clear all contents

   @return this DataContainer (for chaining methods)
   */
  public JsonapiPayload clear() {
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
  public Collection<JsonapiPayloadObject> getDataMany() {
    return dataMany;
  }

  /**
   Set has-many data

   @param payloadObjects to set
   @return this Payload (for chaining methods)
   */
  public <N extends JsonapiPayloadObject> JsonapiPayload setDataMany(Collection<N> payloadObjects) {
    this.setDataType(PayloadDataType.Many);
    payloadObjects.forEach(this::addData);
    return this;
  }

  /**
   Get has-one data

   @return one data resource object
   */
  public Optional<JsonapiPayloadObject> getDataOne() {
    if (!PayloadDataType.One.equals(dataType)) return Optional.empty();
    if (Objects.isNull(dataOne)) return Optional.empty();
    return Optional.of(dataOne);
  }

  /**
   Set has-one data

   @param payloadObject to set
   @return this Payload (for chaining methods)
   */
  public <N extends JsonapiPayloadObject> JsonapiPayload setDataOne(N payloadObject) {
    dataOne = payloadObject;
    dataType = PayloadDataType.One;
    return this;
  }

  /**
   Get container type, ambiguous, has-one, or has-many

   @return container type
   */
  public PayloadDataType getDataType() {
    return dataType;
  }

  /**
   Set data type

   @param dataType to set
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload setDataType(PayloadDataType dataType) {
    this.dataType = dataType;
    return this;
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
  public Collection<JsonapiPayloadObject> getIncluded() {
    return included;
  }

  /**
   Set all included

   @param included to set
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload setIncluded(Collection<JsonapiPayloadObject> included) {
    this.included.clear();
    this.included.addAll(included);
    return this;
  }

  /**
   Get included resource objects matching a specified type

   @return included resource objects matching specified type
   */
  public Collection<JsonapiPayloadObject> getIncludedOfType(String resourceType) {
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
   Set all links

   @param links to set
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload setLinks(Map<String, String> links) {
    this.links.clear();
    this.links.putAll(links);
    return this;
  }

  /**
   Get URI link to self

   @return URI of self
   */
  public Optional<URI> getSelfURI() {
    return links.containsKey(KEY_SELF) ?
      Optional.of(URI.create(links.get(KEY_SELF))) :
      Optional.empty();
  }

  /**
   Set URI link to self

   @param uri link to set
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload setSelfURI(URI uri) {
    links.put(KEY_SELF, uri.toString());
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
   Set Data as reference to one type and id

   @param type to reference
   @param id   to reference
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload setDataReference(String type, String id) {
    return setDataOne(new JsonapiPayloadObject().setType(type).setId(id));
  }

  /**
   Add to included

   @param included to add
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload addToIncluded(JsonapiPayloadObject included) {
    this.included.add(included);
    return this;
  }

  /**
   Add all to included

   @param included to add
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload addAllToIncluded(Collection<JsonapiPayloadObject> included) {
    this.included.addAll(included);
    return this;
  }

  /**
   Add a payload object to the data many

   @param jsonapiPayloadObject to add
   @return this Payload (for chaining methods)
   */
  public JsonapiPayload addToDataMany(JsonapiPayloadObject jsonapiPayloadObject) {
    setDataType(PayloadDataType.Many);
    dataMany.add(jsonapiPayloadObject);
    return this;
  }

  /**
   Test if the Payload has-many data, with the specified class + ids

   @param resourceType to test
   @param resourceIds  to test
   @return true if payload matches the specified HasMany resource type and ids
   */
  public boolean hasDataMany(String resourceType, Collection<String> resourceIds) {
    if (!PayloadDataType.Many.equals(dataType)) return false;
    if (!Objects.equals(resourceIds.size(), dataMany.size())) return false;
    Collection<String> foundIds = Lists.newArrayList();
    for (JsonapiPayloadObject jsonapiPayloadObject : dataMany) {
      if (foundIds.contains(jsonapiPayloadObject.getId())) return false;
      if (!Objects.equals(resourceType, jsonapiPayloadObject.getType())) return false;
      if (!resourceIds.contains(jsonapiPayloadObject.getId())) return false;
      foundIds.add(jsonapiPayloadObject.getId());
    }
    return true;
  }

  /**
   Test if the Payload has-many data, with an empty set

   @return true if payload is empty HasMany
   */
  public boolean hasDataManyEmpty() {
    if (!PayloadDataType.Many.equals(dataType)) return false;
    return 0 == dataMany.size();
  }

  /**
   Test if the Payload has-one data, with the specified class + id

   @param resourceType to test
   @param resourceId   to test
   @return true if the Payload has-one data, with the specified class + id
   */
  public boolean hasDataOne(String resourceType, String resourceId) {
    if (!PayloadDataType.One.equals(dataType)) return false;
    Optional<JsonapiPayloadObject> dataOne = getDataOne();
    if (dataOne.isEmpty()) return false;
    if (!Objects.equals(resourceType, dataOne.get().getType())) return false;
    return Objects.equals(resourceId, dataOne.get().getId());
  }

  /**
   Test if the Payload has-one data, with the specified class + id

   @param resourceType to test
   @param resourceIds  to test
   @return true if the Payload has-one data, with the specified class + id
   */
  public boolean hasDataOne(String resourceType, Collection<String> resourceIds) {
    if (!PayloadDataType.One.equals(dataType)) return false;
    Optional<JsonapiPayloadObject> dataOne = getDataOne();
    if (dataOne.isEmpty()) return false;
    if (!Objects.equals(resourceType, dataOne.get().getType())) return false;
    return resourceIds.contains(dataOne.get().getId());
  }

  /**
   Test if the Payload has-one data, with the specified class + id

   @param resource to test
   @return true if the Payload has-one data, with the specified class + id
   */
  public <N> boolean hasDataOne(N resource) {
    if (!PayloadDataType.One.equals(dataType)) return false;
    Optional<JsonapiPayloadObject> dataOne = getDataOne();
    return dataOne.map(payloadObject -> payloadObject.isSame(resource)).orElse(false);
  }

  /**
   Test if the Payload has-one data, with empty (null) specified

   @return true if the Payload has-one data, with empty (null) specified
   */
  public boolean hasDataOneEmpty() {
    if (!PayloadDataType.One.equals(dataType)) return false;
    Optional<JsonapiPayloadObject> dataOne = getDataOne();
    return dataOne.isEmpty();
  }

  /**
   Test if has specified number of errors

   @param errorCount to test
   @return true if has specified number of errors
   */
  public boolean hasErrorCount(int errorCount) {
    return Objects.equals(errorCount, getErrors().size());
  }

  /**
   Test if has included entity, and return a payload object testion utility to make testions about it

   @param resource to test is included
   @param <N>      type of resource
   @return true if has included entity, and return a payload object testion utility to make testions about it
   */
  public <N> boolean hasIncluded(N resource) {
    Optional<JsonapiPayloadObject> payloadObject = getIncluded().stream().filter(obj -> obj.isSame(resource)).findFirst();
    return payloadObject.isPresent();
  }

  /**
   Test has included entities

   @param resourceType to test
   @param resources    to test
   @return this Payload testion utility (for chaining methods)
   */
  public <N> boolean hasIncluded(String resourceType, ImmutableList<N> resources) {
    try {
      Collection<JsonapiPayloadObject> included = getIncludedOfType(resourceType);
      if (!Objects.equals(resources.size(), included.size())) return false;
      Collection<String> foundIds = Lists.newArrayList();
      Collection<String> resourceIds = new ArrayList<>();
      for (N resource : resources) {
        var resourceId = Entities.getId(resource);
        for (JsonapiPayloadObject jsonapiPayloadObject : included) {
          if (foundIds.contains(jsonapiPayloadObject.getId())) return false;
          if (!Objects.equals(resourceType, jsonapiPayloadObject.getType())) return false;
          if (!resourceIds.contains(jsonapiPayloadObject.getId())) return false;
          foundIds.add(jsonapiPayloadObject.getId());
        }
        if (Objects.nonNull(resourceId))
          resourceIds.add(resourceId.toString());
      }
      return true;
    } catch (EntityException e) {
      return false;
    }
  }
}
