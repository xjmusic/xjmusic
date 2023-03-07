// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.notification;

import com.google.common.collect.ImmutableMap;
import io.xj.lib.app.AppEnvironment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class NotificationProviderImplTest {
  private final AppEnvironment env = AppEnvironment.from(ImmutableMap.of(
    "AWS_ACCESS_KEY_ID", "AKIALKSFDJKGIOURTJ7H",
    "AWS_SECRET_KEY", "jhfd897+jkhjHJJDKJF/908090JHKJJHhjhfg78h"
  ));

  @Test
  public void instantiate() {
    NotificationProvider subject = new NotificationProviderImpl(env);

    assertNotNull(subject);
  }
}
