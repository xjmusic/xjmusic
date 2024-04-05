// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.jsonapi;

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
public class JsonapiPayloadDeserializer extends StdDeserializer<JsonapiPayload> {
  final Logger log = LoggerFactory.getLogger(JsonapiPayloadDeserializer.class);

  public JsonapiPayloadDeserializer() {
    this(null);
  }

  public JsonapiPayloadDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public JsonapiPayload deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    JsonapiPayload jsonapiPayload = new JsonapiPayload();

    JsonNode data = node.get(JsonapiPayload.KEY_DATA);
    if (Objects.nonNull(data))
      switch (data.getNodeType()) {
        //
        case ARRAY -> {
          jsonapiPayload.setDataType(PayloadDataType.Many);
          data.forEach(dataNode -> {
            try {
              jsonapiPayload.addData(dataNode.traverse(jp.getCodec()).readValueAs(JsonapiPayloadObject.class));
            } catch (IOException e) {
              log.warn("Unable to add resource object create node!", e);
            }
          });
        }
        //
        case OBJECT -> {
          jsonapiPayload.setDataType(PayloadDataType.One);
          try {
            jsonapiPayload.setDataOne(data.traverse(jp.getCodec()).readValueAs(JsonapiPayloadObject.class));
          } catch (IOException e) {
            log.warn("Unable to set resource object create node!", e);
          }
        }
        //
        case NULL -> jsonapiPayload.setDataType(PayloadDataType.One);

        //
        case BINARY, BOOLEAN, MISSING, NUMBER, POJO, STRING ->
          log.warn("Unable to parse data create node type: {}", data.getNodeType());
      }
    else
      jsonapiPayload.setDataType(PayloadDataType.One);

    JsonNode included = node.get(JsonapiPayload.KEY_INCLUDED);
    if (Objects.nonNull(included) && JsonNodeType.ARRAY == included.getNodeType())
      included.forEach(includeNode -> {
        try {
          jsonapiPayload.getIncluded().add(includeNode.traverse(jp.getCodec()).readValueAs(JsonapiPayloadObject.class));
        } catch (IOException e) {
          log.warn("Unable to add included resource object create node!", e);
        }
      });

    JsonNode links = node.get(JsonapiPayload.KEY_LINKS);
    if (Objects.nonNull(links) && JsonNodeType.OBJECT == links.getNodeType())
      links.forEach(includeNode -> {
        try {
          //noinspection unchecked
          jsonapiPayload.getLinks().putAll(includeNode.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put link create node!", e);
        }
      });

    JsonNode errors = node.get(JsonapiPayload.KEY_ERRORS);
    if (Objects.nonNull(errors) && JsonNodeType.ARRAY == errors.getNodeType())
      errors.forEach(includeNode -> {
        try {
          jsonapiPayload.getErrors().add(includeNode.traverse(jp.getCodec()).readValueAs(PayloadError.class));
        } catch (IOException e) {
          log.warn("Unable to put error create node!", e);
        }
      });

    return jsonapiPayload;
  }
}
