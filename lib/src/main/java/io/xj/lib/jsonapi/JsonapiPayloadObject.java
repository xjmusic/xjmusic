// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
  private final Map<String, Object> attributes = Maps.newHashMap();
  private final Map<String, String> links = Maps.newHashMap();
  private final Map<String, JsonapiPayload> relationships = Maps.newHashMap();
  private String type;
  private String id;

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
   Get the primary object of a relationship payload

   @param relationshipName to get payload for, and extract many payload resource object
   @return (optional) payload resource object if found, or empty
   */
  public Collection<JsonapiPayloadObject> getRelationshipDataMany(String relationshipName) {
    if (!getRelationships().containsKey(relationshipName))
      return Lists.newArrayList();

    return getRelationships().get(relationshipName).getDataMany();
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
    this.type = Entities.toType(type);
    return this;
  }

  /**
   set Type using an entity's class

   @param type class of type to set
   @return this PayloadObject (for chaining methods)
   */
  public JsonapiPayloadObject setType(Class<?> type) {
    this.type = Entities.toType(type);
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
      if (!Objects.equals(getType(), Entities.toType(resource))) return false;
      Optional<Object> id = Entities.get(resource, KEY_ID);
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

   @param parentType of relationship
   @param parentIds  of relationship
   @return true if this payload object has the requested belongs-to relationship
   */
  public boolean belongsTo(Class<?> parentType, Collection<String> parentIds) {
    String key = Entities.toBelongsTo(type);
    return relationships.containsKey(key) &&
      relationships.get(key).hasDataOne(Entities.toType(type), parentIds);
  }

  /**
   Test whether this payload object has the requested belongs-to relationship

   @param type of relationship
   @param id   of relationship
   @return true if this payload object has the requested belongs-to relationship
   */
  public boolean belongsTo(String type, String id) {
    String key = Entities.toBelongsTo(type);
    return relationships.containsKey(key) &&
      relationships.get(key).hasDataOne(Entities.toType(type), id);
  }

  /**
   Test whether this payload object has the requested belongs-to relationship

   @param resource to assert belongs-to
   @return true if this payload object has the requested belongs-to relationship
   */
  public <N> boolean belongsTo(N resource) {
    try {
      var resourceId = Entities.getId(resource);
      if (Objects.isNull(resourceId)) return false;
      return belongsTo(Entities.toType(resource), resourceId.toString());
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
      String key = Entities.toHasMany(type);
      if (!relationships.containsKey(key)) return false;
      List<String> list = new ArrayList<>();
      for (N resource : resources) {
        var resourceId = Entities.getId(resource);
        if (Objects.nonNull(resourceId))
          list.add(resourceId.toString());
      }
      return relationships.get(key).hasDataMany(Entities.toType(type), list);
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
    return this.type.equals(Entities.toType(type));
  }

  /**
   Check whether an attribute value matches the given Long value

   @param attrName to check value of
   @param value    to check for equality to
   @return true if attribute value is equal to specified Long value
   */
  public boolean isAttrEqual(String attrName, Long value) {
    try {
      return Objects.nonNull(value) &&
        value.equals(Long.valueOf(String.valueOf(getAttribute(attrName))));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   Check whether an attribute value matches the given String value

   @param attrName to check value of
   @param value    to check for equality to
   @return true if attribute value is equal to specified String value
   */
  public boolean isAttrEqual(String attrName, String value) {
    try {
      return Objects.nonNull(value) &&
        value.equals(String.valueOf(getAttribute(attrName)));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   Check whether an attribute value, parsed as an Instant, is before the given Instant value

   @param attrName to check value of
   @param value    to check for Instant before
   @return true if attribute value, parsed as an Instant, is before the given Instant value
   */
  public boolean isAttrBefore(String attrName, Instant value) {
    try {
      return Objects.nonNull(value) &&
        Instant.parse(String.valueOf(getAttribute(attrName))).isBefore(value);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   Check whether an attribute value is null

   @param attrName to check for null
   @return true if attribute value is null
   */
  public boolean isAttrNull(String attrName) {
    return Objects.isNull(getAttribute(attrName));
  }


  /**
   Get the Long value with the given attribute name

   @param attrName to get long value of
   @return long value of given attribute name
   */
  public Long getAttrLongValue(String attrName) {
    return Long.valueOf(String.valueOf(getAttribute(attrName).orElse(-1L)));
  }

  /**
   Get the Instant value with the given attribute name

   @param attrName to get instant value of
   @return instant value of given attribute name
   */
  public Instant getAttrInstantValue(String attrName) {
    try {
      return Instant.parse(String.valueOf(getAttribute(attrName).orElseThrow()));
    } catch (Exception e) {
      return Instant.MIN;
    }
  }
}
