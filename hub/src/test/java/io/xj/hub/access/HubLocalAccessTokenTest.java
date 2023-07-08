// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@SpringBootTest
public class HubLocalAccessTokenTest {
  HubAccessTokenGenerator hubAccessTokenGenerator;

  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @BeforeEach
  public void setUp() throws Exception {
    hubAccessTokenGenerator = new HubAccessTokenGeneratorImpl();
  }

  @Test
  public void generate_UniqueTokens() {
    String t1 = hubAccessTokenGenerator.generate();
    String t2 = hubAccessTokenGenerator.generate();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }
}
