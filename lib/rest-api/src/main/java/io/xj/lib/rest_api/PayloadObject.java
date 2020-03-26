// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 Object in a Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
@JsonDeserialize(using = PayloadObjectDeserializer.class)
@JsonSerialize(using = PayloadObjectSerializer.class)
public class PayloadObject {
  public static final String KEY_LINKS = "links";
  public static final String KEY_RELATIONSHIPS = "relationships";
  public static final String KEY_ATTRIBUTES = "attributes";
  public static final String KEY_ID = "id";
  public static final String KEY_TYPE = "type";
  private final Map<String, Object> attributes = Maps.newHashMap();
  private final Map<String, String> links = Maps.newHashMap();
  private final Map<String, Payload> relationships = Maps.newHashMap();
  private String type;
  private String id;

  /**
   Add a relationship object to this resource object

   @param relationshipName for which object will be added
   @param payload          to add
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject add(String relationshipName, Payload payload) {
    relationships.put(relationshipName, payload);
    return this;
  }

  /**
   Get attribute by name, if it exists

   @param name of attribute to get
   @return value of attribute (optional), else empty
   */
  public Optional<Object> getAttribute(String name) {
    return attributes.containsKey(name) ?
            Optional.of(attributes.get(name)) :
            Optional.empty();
  }

  /**
   get Attributes

   @return Attributes
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   get Id

   @return Id
   */
  public String getId() {
    return id;
  }

  /**
   get Links

   @return Links
   */
  public Map<String, String> getLinks() {
    return links;
  }

  /**
   Get the primary object of a relationship payload

   @param relationshipName to get payload for, and extract one payload resource object
   @return (optional) payload resource object if found, or empty
   */
  public Optional<PayloadObject> getRelationshipDataOne(String relationshipName) {
    if (!getRelationships().containsKey(relationshipName))
      return Optional.empty();

    if (getRelationships().get(relationshipName).getDataOne().isEmpty())
      return Optional.empty();

    return getRelationships().get(relationshipName).getDataOne();
  }

  /**
   Get the primary object of a relationship payload

   @param relationshipName to get payload for, and extract many payload resource object
   @return (optional) payload resource object if found, or empty
   */
  public Collection<PayloadObject> getRelationshipDataMany(String relationshipName) {
    if (!getRelationships().containsKey(relationshipName))
      return Lists.newArrayList();

    return getRelationships().get(relationshipName).getDataMany();
  }

  /**
   get Relationships

   @return Relationships
   */
  public Map<String, Payload> getRelationships() {
    return relationships;
  }

  /**
   get Type

   @return Type
   */
  public String getType() {
    return type;
  }

  /**
   Set an attribute by name to a specified value

   @param name  of attribute
   @param value to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setAttribute(String name, Object value) {
    this.attributes.put(name, value);
    return this;
  }

  /**
   set Attributes

   @param attributes to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setAttributes(Map<String, Object> attributes) {
    this.attributes.clear();
    this.attributes.putAll(attributes);
    return this;
  }

  /**
   set Id

   @param id to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setId(String id) {
    this.id = id;
    return this;
  }

  /**
   set Links

   @param links to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setLinks(Map<String, String> links) {
    this.links.clear();
    this.links.putAll(links);
    return this;
  }

  /**
   set Relationships

   @param payloadMap to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setRelationships(Map<String, Payload> payloadMap) {
    relationships.clear();
    relationships.putAll(payloadMap);
    return this;
  }

  /**
   set Type

   @param type to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setType(String type) {
    this.type = PayloadKey.toResourceType(type);
    return this;
  }

  /**
   set Type using an entity's class

   @param type class of type to set
   @return this PayloadObject (for chaining methods)
   */
  public PayloadObject setType(Class<?> type) {
    this.type = PayloadKey.toResourceType(type);
    return this;
  }

  /**
   Is the provided resource the same entity (type + id) as this payload object?

   @param resource to match
   @param <N>      type of resource
   @return true if match
   */
  public <N> boolean isSame(N resource) {
    try {
      if (!Objects.equals(getType(), PayloadKey.toResourceType(resource))) return false;
      Optional<Object> id = PayloadEntity.get(resource, KEY_ID);
      return id.isPresent() && Objects.equals(getId(), String.valueOf(id.get()));
    } catch (RestApiException ignored) {
      return false;
    }
  }

  /**
   Strip this payload object down to only the type and id

   @return minimal payload object
   */
  public PayloadObject toMinimal() {
    return new PayloadObject()
            .setType(type)
            .setId(id);
  }

}
