// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.net.URI;
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
@JsonDeserialize(using = PayloadDeserializer.class)
@JsonSerialize(using = PayloadSerializer.class)
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
  public Optional<URI> getSelfURI() {
    return links.containsKey(KEY_SELF) ?
            Optional.of(URI.create(links.get(KEY_SELF))) :
            Optional.empty();
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
   Set Data as reference to one type and id

   @param type to reference
   @param id   to reference
   @return this Payload (for chaining methods)
   */
  public Payload setDataReference(String type, String id) {
    return setDataOne(new PayloadObject().setType(type).setId(id));
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

  /**
   Add a payload object to the data many

   @param payloadObject to add
   @return this Payload (for chaining methods)
   */
  public Payload addToDataMany(PayloadObject payloadObject) {
    setDataType(PayloadDataType.HasMany);
    dataMany.add(payloadObject);
    return this;
  }
}
