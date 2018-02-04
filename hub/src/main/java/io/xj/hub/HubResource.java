// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.craft.CraftModule;

public class HubResource {
  public static final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
}
