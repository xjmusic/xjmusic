//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.app.AppConfiguration;
import io.xj.core.testing.AppTestConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class GsonProviderImplTest {
  GsonProvider subject;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    subject = injector.getInstance(GsonProvider.class);
  }

  @Test
  public void gson() {
    assertSame(Gson.class, subject.gson().getClass());
  }

}
