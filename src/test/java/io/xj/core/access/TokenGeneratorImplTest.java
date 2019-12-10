// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.app.AppConfiguration;
import io.xj.core.testing.AppTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(MockitoJUnitRunner.class)
public class TokenGeneratorImplTest {
  private TokenGenerator tokenGenerator;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
        }
      })));

    tokenGenerator = injector.getInstance(TokenGenerator.class);
  }

  @Test
  public void generate_UniqueTokens() {
    String t1 = tokenGenerator.generate();
    String t2 = tokenGenerator.generate();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }

  @Test
  public void generate_UniqueShortTokens() {
    String t1 = tokenGenerator.generateShort();
    String t2 = tokenGenerator.generateShort();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }

}
