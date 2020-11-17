// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.pubsub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class PubSubProviderImplTest {

  @Test
  public void instantiate() {
    Config config = ConfigFactory.parseResources("default.conf")
      .withValue("aws.accessKeyID", ConfigValueFactory.fromAnyRef("AKIALKSFDJKGIOURTJ7H"))
      .withValue("aws.secretKey", ConfigValueFactory.fromAnyRef("jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h"));
    Injector injector = Guice.createInjector(ImmutableSet.of(Modules.override(new PubSubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
        }
      })));

    PubSubProvider subject = injector.getInstance(PubSubProvider.class);

    assertNotNull(subject);
  }
}
