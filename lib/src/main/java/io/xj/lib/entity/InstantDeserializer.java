// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.entity;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;

@JsonDeserialize(using = InstantDeserializer.class, as = Instant.class)
public class InstantDeserializer extends StdDeserializer<Instant> {

  public InstantDeserializer() {
    this(null);
  }

  public InstantDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Instant deserialize(JsonParser jp, DeserializationContext context) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return Instant.parse(node.asText());
  }
}
