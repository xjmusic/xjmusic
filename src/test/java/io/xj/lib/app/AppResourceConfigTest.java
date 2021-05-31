// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.app;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.RuntimeType;

import static org.junit.Assert.assertEquals;

public class AppResourceConfigTest {
  private AppResourceConfig subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector();
    subject = new AppResourceConfig(injector, ImmutableSet.of());
  }

  @Test
  public void getRuntimeType() {
    assertEquals(RuntimeType.SERVER, subject.getRuntimeType());
  }
}
