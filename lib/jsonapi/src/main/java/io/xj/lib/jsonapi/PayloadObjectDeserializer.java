// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class PayloadObjectDeserializer extends StdDeserializer<PayloadObject> {
  final Logger log = LoggerFactory.getLogger(PayloadObjectDeserializer.class);

  public PayloadObjectDeserializer() {
    this(null);
  }

  public PayloadObjectDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public PayloadObject deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    PayloadObject obj = new PayloadObject();
    ObjectMapper mapper = new ObjectMapper();


    JsonNode id = node.get(PayloadObject.KEY_ID);
    if (Objects.nonNull(id))
      obj.setId(id.asText());

    JsonNode type = node.get(PayloadObject.KEY_TYPE);
    if (Objects.nonNull(type))
      obj.setType(type.asText());

    JsonNode links = node.get(PayloadObject.KEY_LINKS);
    if (Objects.nonNull(links) && JsonNodeType.OBJECT == links.getNodeType())
      links.forEach(includeNode -> {
        try {
          obj.getLinks().putAll(links.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put link create node!", e);
        }
      });

    JsonNode attributes = node.get(PayloadObject.KEY_ATTRIBUTES);
    if (Objects.nonNull(attributes) && JsonNodeType.OBJECT == attributes.getNodeType())
      attributes.forEach(includeNode -> {
        try {
          obj.getAttributes().putAll(attributes.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put attribute create node!", e);
        }
      });

    JsonNode relationships = node.get(PayloadObject.KEY_RELATIONSHIPS);
    if (Objects.nonNull(relationships) && JsonNodeType.OBJECT == relationships.getNodeType()) {
      TypeReference<Map<String, Payload>> typeRef = new TypeReference<>() {
      };
      obj.getRelationships().putAll(relationships.traverse(jp.getCodec()).readValueAs(typeRef));
    }

    return obj;
  }
}
