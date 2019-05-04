// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import io.xj.craft.CraftModule;

public class HubResource {
  public static final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  public static final GsonProvider gsonProvider = injector.getInstance(GsonProvider.class);
}
