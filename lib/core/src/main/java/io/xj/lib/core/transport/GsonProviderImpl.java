// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.transport;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.core.CoreGsonBuilder;

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
