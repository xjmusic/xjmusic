// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

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
 Deserializer for a Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 <p>
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public class PayloadDeserializer extends StdDeserializer<Payload> {
  final Logger log = LoggerFactory.getLogger(PayloadDeserializer.class);

  public PayloadDeserializer() {
    this(null);
  }

  public PayloadDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Payload deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    Payload payload = new Payload();

    JsonNode data = node.get(Payload.KEY_DATA);
    switch (data.getNodeType()) {
      //
      case ARRAY:
        payload.setDataType(PayloadDataType.HasMany);
        data.forEach(dataNode -> {
          try {
            payload.addData(dataNode.traverse(jp.getCodec()).readValueAs(PayloadObject.class));
          } catch (IOException e) {
            log.warn("Unable to add resource object create node!", e);
          }
        });
        break;
      //
      case OBJECT:
        payload.setDataType(PayloadDataType.HasOne);
        try {
          payload.setDataOne(data.traverse(jp.getCodec()).readValueAs(PayloadObject.class));
        } catch (IOException e) {
          log.warn("Unable to set resource object create node!", e);
        }
        break;
      //
      case NULL:
        payload.setDataType(PayloadDataType.HasOne);
        break;
      //
      case BINARY:
      case BOOLEAN:
      case MISSING:
      case NUMBER:
      case POJO:
      case STRING:
        log.warn("Unable to parse data create node type: {}", data.getNodeType());
        break;
    }

    JsonNode included = node.get(Payload.KEY_INCLUDED);
    if (Objects.nonNull(included) && JsonNodeType.ARRAY == included.getNodeType())
      included.forEach(includeNode -> {
        try {
          payload.getIncluded().add(includeNode.traverse(jp.getCodec()).readValueAs(PayloadObject.class));
        } catch (IOException e) {
          log.warn("Unable to add included resource object create node!", e);
        }
      });

    JsonNode links = node.get(Payload.KEY_LINKS);
    if (Objects.nonNull(links) && JsonNodeType.OBJECT == links.getNodeType())
      links.forEach(includeNode -> {
        try {
          payload.getLinks().putAll(includeNode.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put link create node!", e);
        }
      });

    JsonNode errors = node.get(Payload.KEY_ERRORS);
    if (Objects.nonNull(errors) && JsonNodeType.ARRAY == errors.getNodeType())
      errors.forEach(includeNode -> {
        try {
          payload.getErrors().add(includeNode.traverse(jp.getCodec()).readValueAs(PayloadError.class));
        } catch (IOException e) {
          log.warn("Unable to put error create node!", e);
        }
      });

    return payload;
  }
}
