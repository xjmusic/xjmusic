//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class GsonProviderImplTest {
  Injector injector = Guice.createInjector(new CoreModule());
  GsonProvider subject;

  @Before
  public void setUp() throws Exception {
    subject = injector.getInstance(GsonProvider.class);
  }

  @Test
  public void gson() {
    assertSame(Gson.class, subject.gson().getClass());
  }

  @Test
  public void wrap() {
    String result = subject.wrap("colors", ImmutableList.of("Red", "Green", "Blue"));

    assertEquals("{\"colors\":[\"Red\",\"Green\",\"Blue\"]}", result);
  }

  @Test
  public void wrap_emptyCollection() {
    String result = subject.wrap("colors", ImmutableList.of());

    assertEquals("{\"colors\":[]}", result);
  }

  @Test
  public void wrapError() {
    String result = subject.wrapError("dang it");

    assertEquals("{\"errors\":[{\"detail\":\"dang it\"}]}", result);
  }
}
