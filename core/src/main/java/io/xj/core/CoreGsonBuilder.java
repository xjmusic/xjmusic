//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
import io.xj.core.model.payload.serializer.PayloadObjectSerializer;
import io.xj.core.model.payload.serializer.PayloadSerializer;
import io.xj.core.model.time.InstantDeserializer;
import io.xj.core.model.time.InstantSerializer;

import java.time.Instant;

/**
 [#166274496] JSON transport implemented purely in Gson via JsonProvider
 */
public class CoreGsonBuilder {
  private final Gson gson;

  /**
   Build a GSON adapter here, in order to centralize this at the base of the core module@param chainFactory
   */
  public CoreGsonBuilder() {
    GsonBuilder g = new GsonBuilder();
    g.serializeNulls();
    registerPrimitiveAdapters(g);
    registerPayloadAdapters(g);
    gson = g.create();
  }

  /**
   Register primitive adapters

   @param g GsonBuilder to register adapters on
   */
  private static void registerPrimitiveAdapters(GsonBuilder g) {
    g.registerTypeHierarchyAdapter(Instant.class, new InstantDeserializer());
    g.registerTypeHierarchyAdapter(Instant.class, new InstantSerializer());
    g.disableInnerClassSerialization();
  }

  /**
   Register payload adapters

   @param g GsonBuilder to register adapters on
   */
  private static void registerPayloadAdapters(GsonBuilder g) {
    g.registerTypeHierarchyAdapter(Payload.class, new PayloadSerializer());
    g.registerTypeHierarchyAdapter(PayloadObject.class, new PayloadObjectSerializer());
  }


  /**
   Create the Gson

   @return Gson
   */
  public Gson getGson() {
    return gson;
  }
}
