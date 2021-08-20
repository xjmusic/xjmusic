// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@JsonSerialize(using = InstantSerializer.class)
public class InstantSerializer extends StdSerializer<Instant> {

  public InstantSerializer() {
    this(null);
  }

  public InstantSerializer(Class<Instant> t) {
    super(t);
  }

  @Override
  public void serialize(Instant value, JsonGenerator json, SerializerProvider provider) throws IOException {
    json.writeObject(value.truncatedTo(ChronoUnit.MICROS).toString());
  }
}
