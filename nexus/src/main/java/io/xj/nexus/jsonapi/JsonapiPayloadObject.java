// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.jsonapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.xj.nexus.entity.EntityException;
import io.xj.nexus.entity.EntityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
@JsonDeserialize(using = JsonapiPayloadObjectDeserializer.class)
@JsonSerialize(using = JsonapiPayloadObjectSerializer.class)
public class JsonapiPayloadObject {
  public static final String KEY_LINKS = "links";
  public static final String KEY_RELATIONSHIPS = "relationships";
  public static final String KEY_ATTRIBUTES = "attributes";
  public static final String KEY_ID = "id";
  public static final String KEY_TYPE = "type";
  final Map<String, Object> attributes = new HashMap<>();
  final Map<String, String> links = new HashMap<>();
  final Map<String, JsonapiPayload> relationships = new HashMap<>();
  String type;
  String id;

  /**
   Add a relationship object to this resource object

   @param relationshipName for which object will be added
   @param jsonapiPayload   to add
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject add(String relationshipName, JsonapiPayload jsonapiPayload) {
    relationships.put(relationshipName, jsonapiPayload);
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
   set Attributes

   @param attributes to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setAttributes(Map<String, Object> attributes) {
    this.attributes.clear();
    this.attributes.putAll(attributes);
    return this;
  }

  /**
   get Id

   @return Id
   */
  public String getId() {
    return id;
  }

  /**
   set Id

   @param id to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setId(String id) {
    this.id = id;
    return this;
  }

  /**
   get Links

   @return Links
   */
  public Map<String, String> getLinks() {
    return links;
  }

  /**
   set Links

   @param links to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setLinks(Map<String, String> links) {
    this.links.clear();
    this.links.putAll(links);
    return this;
  }

  /**
   Get the primary object of a relationship payload

   @param relationshipName to get payload for, and extract one payload resource object
   @return (optional) payload resource object if found, or empty
   */
  public Optional<JsonapiPayloadObject> getRelationshipDataOne(String relationshipName) {
    if (!getRelationships().containsKey(relationshipName))
      return Optional.empty();

    if (getRelationships().get(relationshipName).getDataOne().isEmpty())
      return Optional.empty();

    return getRelationships().get(relationshipName).getDataOne();
  }

  /**
   get Relationships

   @return Relationships
   */
  public Map<String, JsonapiPayload> getRelationships() {
    return relationships;
  }

  /**
   set Relationships

   @param payloadMap to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setRelationships(Map<String, JsonapiPayload> payloadMap) {
    relationships.clear();
    relationships.putAll(payloadMap);
    return this;
  }

  /**
   get Type

   @return Type
   */
  public String getType() {
    return type;
  }

  /**
   set Type

   @param type to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setType(String type) {
    this.type = EntityUtils.toType(type);
    return this;
  }

  /**
   set Type using an entity's class

   @param type class of type to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setType(Class<?> type) {
    this.type = EntityUtils.toType(type);
    return this;
  }

  /**
   Set an attribute by name to a specified value

   @param name  of attribute
   @param value to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setAttribute(String name, Object value) {
    this.attributes.put(name, value);
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
      if (!Objects.equals(getType(), EntityUtils.toType(resource))) return false;
      Optional<Object> id = EntityUtils.get(resource, KEY_ID);
      return id.isPresent() && Objects.equals(getId(), String.valueOf(id.get()));
    } catch (EntityException ignored) {
      return false;
    }
  }

  /**
   Strip this payload object down to only the type and id

   @return minimal payload object
   */
  public JsonapiPayloadObject toMinimal() {
    return new JsonapiPayloadObject()
      .setType(type)
      .setId(id);
  }

  /**
   Test whether this payload object has the requested belongs-to relationship

   @param type of relationship
   @param id   of relationship
   @return true if this payload object has the requested belongs-to relationship
   */
  public boolean belongsTo(Class<?> type, String id) {
    return belongsTo(type.getSimpleName(), id);
  }

  /**
   Test whether this payload object has the requested belongs-to relationship

   @param type of relationship
   @param id   of relationship
   @return true if this payload object has the requested belongs-to relationship
   */
  public boolean belongsTo(String type, String id) {
    String key = EntityUtils.toBelongsTo(type);
    return relationships.containsKey(key) &&
      relationships.get(key).hasDataOne(EntityUtils.toType(type), id);
  }

  /**
   Test whether this payload object has the requested belongs-to relationship

   @param resource to assert belongs-to
   @return true if this payload object has the requested belongs-to relationship
   */
  public <N> boolean belongsTo(N resource) {
    try {
      var resourceId = EntityUtils.getId(resource);
      if (Objects.isNull(resourceId)) return false;
      return belongsTo(EntityUtils.toType(resource), resourceId.toString());
    } catch (EntityException e) {
      return false;
    }
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> boolean hasMany(Class<?> type, Collection<N> resources) throws JsonapiException {
    return hasMany(type.getSimpleName(), resources);
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> boolean hasMany(String type, Collection<N> resources) throws JsonapiException {
    try {
      String key = EntityUtils.toHasMany(type);
      if (!relationships.containsKey(key)) return false;
      List<String> list = new ArrayList<>();
      for (N resource : resources) {
        var resourceId = EntityUtils.getId(resource);
        if (Objects.nonNull(resourceId))
          list.add(resourceId.toString());
      }
      return relationships.get(key).hasDataMany(EntityUtils.toType(type), list);
    } catch (EntityException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert whether payload object is the same type as the provided

   @param type to assert sameness of
   @return true if same type as provided
   */
  public boolean isType(Class<?> type) {
    return this.type.equals(EntityUtils.toType(type));
  }
}
