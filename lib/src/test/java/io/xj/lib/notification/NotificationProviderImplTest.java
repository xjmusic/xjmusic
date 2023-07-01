// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class NotificationProviderImplTest {

  @Test
  public void instantiate() {
    NotificationProvider subject = new NotificationProviderImpl("","","","");

    assertNotNull(subject);
  }
}
