// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.notification;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class NotificationProviderImplTest {

  @Test
  public void instantiate() {
    var env = Environment.from(ImmutableMap.of(
      "AWS_ACCESS_KEY_ID", "AKIALKSFDJKGIOURTJ7H",
      "AWS_SECRET_KEY", "jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h"
    ));
    var injector = Guice.createInjector(ImmutableSet.of(Modules.override(new NotificationModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
        }
      })));

    NotificationProvider subject = injector.getInstance(NotificationProvider.class);

    assertNotNull(subject);
  }
}
