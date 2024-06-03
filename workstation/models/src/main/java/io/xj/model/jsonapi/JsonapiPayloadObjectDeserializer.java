// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.jsonapi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 Deserializer for an Object in a Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public class JsonapiPayloadObjectDeserializer extends StdDeserializer<JsonapiPayloadObject> {
  final Logger log = LoggerFactory.getLogger(JsonapiPayloadObjectDeserializer.class);

  public JsonapiPayloadObjectDeserializer() {
    this(null);
  }

  public JsonapiPayloadObjectDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public JsonapiPayloadObject deserialize(JsonParser jp, DeserializationContext context)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    JsonapiPayloadObject obj = new JsonapiPayloadObject();

    JsonNode id = node.get(JsonapiPayloadObject.KEY_ID);
    if (Objects.nonNull(id))
      obj.setId(id.asText());

    JsonNode type = node.get(JsonapiPayloadObject.KEY_TYPE);
    if (Objects.nonNull(type))
      obj.setType(type.asText());

    JsonNode links = node.get(JsonapiPayloadObject.KEY_LINKS);
    if (Objects.nonNull(links) && JsonNodeType.OBJECT == links.getNodeType())
      links.forEach(includeNode -> {
        try {
          //noinspection unchecked
          obj.getLinks().putAll(links.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put link create node!", e);
        }
      });

    JsonNode attributes = node.get(JsonapiPayloadObject.KEY_ATTRIBUTES);
    if (Objects.nonNull(attributes) && JsonNodeType.OBJECT == attributes.getNodeType())
      attributes.forEach(includeNode -> {
        try {
          //noinspection unchecked
          obj.getAttributes().putAll(attributes.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put attribute create node!", e);
        }
      });

    JsonNode relationships = node.get(JsonapiPayloadObject.KEY_RELATIONSHIPS);
    if (Objects.nonNull(relationships) && JsonNodeType.OBJECT == relationships.getNodeType()) {
      TypeReference<Map<String, JsonapiPayload>> typeRef = new TypeReference<>() {
      };
      obj.getRelationships().putAll(relationships.traverse(jp.getCodec()).readValueAs(typeRef));
    }

    return obj;
  }
}
