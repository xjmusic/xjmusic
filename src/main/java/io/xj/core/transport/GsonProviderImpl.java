// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.core.CoreGsonBuilder;
import io.xj.core.transport.GsonProvider;

@Singleton
class GsonProviderImpl implements GsonProvider {
  private final Gson gson;

  /**
   Initialize, and register all adapters@param segmentFactory
   */
  @Inject
  public GsonProviderImpl() {
    gson = new CoreGsonBuilder().getGson();
  }

  @Override
  public Gson gson() {
    return gson;
  }

}
