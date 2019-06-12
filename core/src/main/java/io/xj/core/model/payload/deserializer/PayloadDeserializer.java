//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.payload.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadDataType;
import io.xj.core.model.payload.PayloadError;
import io.xj.core.model.payload.PayloadObject;
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
            log.warn("Unable to add resource object from node!", e);
          }
        });
        break;
      //
      case OBJECT:
        payload.setDataType(PayloadDataType.HasOne);
        try {
          payload.setDataOne(data.traverse(jp.getCodec()).readValueAs(PayloadObject.class));
        } catch (IOException e) {
          log.warn("Unable to set resource object from node!", e);
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
        log.warn("Unable to parse data of node type: {}", data.getNodeType());
        break;
    }

    JsonNode included = node.get(Payload.KEY_INCLUDED);
    if (Objects.nonNull(included) && JsonNodeType.ARRAY == included.getNodeType())
      included.forEach(includeNode -> {
        try {
          payload.getIncluded().add(includeNode.traverse(jp.getCodec()).readValueAs(PayloadObject.class));
        } catch (IOException e) {
          log.warn("Unable to add included resource object from node!", e);
        }
      });

    JsonNode links = node.get(Payload.KEY_LINKS);
    if (Objects.nonNull(links) && JsonNodeType.OBJECT == links.getNodeType())
      links.forEach(includeNode -> {
        try {
          payload.getLinks().putAll(includeNode.traverse(jp.getCodec()).readValueAs(Map.class));
        } catch (IOException e) {
          log.warn("Unable to put link from node!", e);
        }
      });

    JsonNode errors = node.get(Payload.KEY_ERRORS);
    if (Objects.nonNull(errors) && JsonNodeType.ARRAY == errors.getNodeType())
      errors.forEach(includeNode -> {
        try {
          payload.getErrors().add(includeNode.traverse(jp.getCodec()).readValueAs(PayloadError.class));
        } catch (IOException e) {
          log.warn("Unable to put error from node!", e);
        }
      });

    return payload;
  }
}
