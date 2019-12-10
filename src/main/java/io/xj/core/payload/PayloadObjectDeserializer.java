// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.payload;

import com.fasterxml.jackson.core.JsonParser;
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
 [#167276586] JSON API facilitates complex transactions
 <p>
 Payloads are Deserialized with Jackson; we're locked into that because that's what Jersey (our REST framework) uses.
 However, Payloads are Serialized with GSON for outbound data, because it's simpler.
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
          obj.getLinks().putAll(includeNode.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put link create node!", e);
        }
      });

    JsonNode attributes = node.get(PayloadObject.KEY_ATTRIBUTES);
    if (Objects.nonNull(attributes) && JsonNodeType.OBJECT == attributes.getNodeType())
      attributes.forEach(includeNode -> {
        try {
          obj.getAttributes().putAll(includeNode.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put attribute create node!", e);
        }
      });

    JsonNode relationships = node.get(PayloadObject.KEY_RELATIONSHIPS);
    if (Objects.nonNull(relationships) && JsonNodeType.OBJECT == relationships.getNodeType())
      relationships.forEach(includeNode -> {
        try {
          obj.getRelationships().putAll(includeNode.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put relationship create node!", e);
        }
      });

    return obj;
  }
}
