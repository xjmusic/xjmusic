// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.token;

import io.xj.core.CoreModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(MockitoJUnitRunner.class)
public class TokenGeneratorImplTest {
  private Injector injector;
  private TokenGenerator tokenGenerator;

  @Before
  public void setUp() throws Exception {
    createInjector();
    tokenGenerator = injector.getInstance(TokenGenerator.class);
  }

  @Test
  public void generate_UniqueTokens() throws Exception {
    String t1 = tokenGenerator.generate();
    String t2 = tokenGenerator.generate();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }

  @Test
  public void generate_UniqueShortTokens() throws Exception {
    String t1 = tokenGenerator.generateShort();
    String t2 = tokenGenerator.generateShort();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
        }
      }));
  }
}
