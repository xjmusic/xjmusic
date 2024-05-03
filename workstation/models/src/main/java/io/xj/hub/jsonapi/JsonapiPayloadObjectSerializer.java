// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.jsonapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 Serializer for an Object in a Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public class JsonapiPayloadObjectSerializer extends StdSerializer<JsonapiPayloadObject> {

  public JsonapiPayloadObjectSerializer() {
    this(null);
  }

  public JsonapiPayloadObjectSerializer(Class<JsonapiPayloadObject> t) {
    super(t);
  }

  @Override
  public void serialize(
    JsonapiPayloadObject value, JsonGenerator json, SerializerProvider provider)
    throws IOException {

    json.writeStartObject();

    // Add id if present
    if (Objects.nonNull(value.getId()) && !value.getId().isEmpty())
      json.writeStringField(JsonapiPayloadObject.KEY_ID, value.getId());

    // Add type if present
    if (Objects.nonNull(value.getType()) && !value.getType().isEmpty())
      json.writeStringField(JsonapiPayloadObject.KEY_TYPE, value.getType());

    // Add links if present
    if (!value.getLinks().isEmpty())
      json.writeObjectField(JsonapiPayloadObject.KEY_LINKS, value.getLinks());

    // Add relationships if present
    if (!value.getRelationships().isEmpty())
      json.writeObjectField(JsonapiPayloadObject.KEY_RELATIONSHIPS, value.getRelationships());

    // Add attributes if present
    if (!value.getAttributes().isEmpty())
      json.writeObjectField(JsonapiPayloadObject.KEY_ATTRIBUTES, value.getAttributes());

    json.writeEndObject();
  }
}
