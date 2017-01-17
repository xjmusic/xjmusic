// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.util.token;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.outright.xj.core.CoreModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Objects;

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
    assert Objects.nonNull(t1);
    assert Objects.nonNull(t2);
    assert !Objects.equals(t1,t2);
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
