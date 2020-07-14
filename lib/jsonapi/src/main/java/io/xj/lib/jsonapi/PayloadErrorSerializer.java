// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Objects;

/**
 Serializer for an Error for a Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 <p>
 Payloads are serialized & deserialized with custom Jackson implementations.
 Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public class PayloadErrorSerializer extends StdSerializer<PayloadError> {

  public PayloadErrorSerializer() {
    this(null);
  }

  public PayloadErrorSerializer(Class<PayloadError> t) {
    super(t);
  }

  @Override
  public void serialize(
    PayloadError value, JsonGenerator json, SerializerProvider provider)
    throws IOException {

    json.writeStartObject();

    // Add id if present
    if (Objects.nonNull(value.getId()) && !value.getId().isEmpty())
      json.writeStringField(PayloadError.KEY_ID, value.getId());

    // Add code if present
    if (Objects.nonNull(value.getCode()) && !value.getCode().isEmpty())
      json.writeStringField(PayloadError.KEY_CODE, value.getCode());

    // Add title if present
    if (Objects.nonNull(value.getTitle()) && !value.getTitle().isEmpty())
      json.writeStringField(PayloadError.KEY_TITLE, value.getTitle());

    // Add detail if present
    if (Objects.nonNull(value.getDetail()) && !value.getDetail().isEmpty())
      json.writeStringField(PayloadError.KEY_DETAIL, value.getDetail());

    // Add links if present
    if (!value.getLinks().isEmpty())
      json.writeObjectField(PayloadError.KEY_LINKS, value.getLinks());

    json.writeEndObject();
  }
}
