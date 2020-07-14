// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 Serializer for a Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public class PayloadSerializer extends StdSerializer<Payload> {

  public PayloadSerializer() {
    this(null);
  }

  public PayloadSerializer(Class<Payload> t) {
    super(t);
  }

  @Override
  public void serialize(
    Payload value, JsonGenerator json, SerializerProvider provider)
    throws IOException {

    json.writeStartObject();

    // Add data (one or many) if present
    if (PayloadDataType.One == value.getDataType() && value.getDataOne().isPresent())
      json.writeObjectField(Payload.KEY_DATA, value.getDataOne().get());

    if (PayloadDataType.Many == value.getDataType()) {
      json.writeArrayFieldStart(Payload.KEY_DATA);
      for (PayloadObject dataItem : value.getDataMany())
        json.writeObject(dataItem);
      json.writeEndArray();
    }

    // Add links if present
    if (!value.getLinks().isEmpty())
      json.writeObjectField(Payload.KEY_LINKS, value.getLinks());

    // Add included if present
    if (!value.getIncluded().isEmpty()) {
      json.writeArrayFieldStart(Payload.KEY_INCLUDED);
      for (PayloadObject dataItem : value.getIncluded())
        json.writeObject(dataItem);
      json.writeEndArray();
    }

    // Add error if present
    if (!value.getErrors().isEmpty()) {
      json.writeArrayFieldStart(Payload.KEY_ERRORS);
      for (PayloadError dataItem : value.getErrors())
        json.writeObject(dataItem);
      json.writeEndArray();
    }

    json.writeEndObject();
  }
}
