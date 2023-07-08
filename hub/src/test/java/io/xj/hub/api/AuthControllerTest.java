// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.manager.UserManager;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.app.AppException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
  @LocalServerPort
  int port;
  @Autowired
  TestRestTemplate restTemplate;
  @SuppressWarnings("unused")
  @MockBean
  PreviewNexusAdmin previewNexusAdmin;
  @MockBean
  UserManager userManager;
  @MockBean
  NotificationProvider notificationProvider;
  @MockBean
  FileStoreProvider fileStoreProvider;
  @MockBean
  HttpClientProvider httpClientProvider;

  HubAccess access;

  @BeforeEach
  public void setUp() throws AppException {
    Account account1 = buildAccount("Testing");
    access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(account1), "User,Artist");
  }

  @Test
  public void getCurrentAuthentication() throws HubAccessException {
    when(userManager.get("testingCookieValue123")).thenReturn(access);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cookie", "access_token=testingCookieValue123");
    HttpEntity<String> entity = new HttpEntity<>(headers);
    ResponseEntity<HubAccess> response = restTemplate.exchange("http://localhost:" + port + "/auth", HttpMethod.GET, entity, HubAccess.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertEquals(access.getUserId(), Objects.requireNonNull(response.getBody()).getUserId());
  }

  @Test
  public void GetAuthGoogle() {
    ResponseEntity<HubAccess> response = restTemplate.exchange("http://localhost:" + port + "/auth/google?state=xj-music", HttpMethod.GET, null, HubAccess.class);

    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    var responseHeaders = response.getHeaders();
    Object redirectLocation = responseHeaders.getFirst("Location");
    assertEquals(
      "https://accounts.google.com/o/oauth2/auth?client_id&redirect_uri=http://localhost/auth/google/callback&response_type=code&scope=profile%20email&state=xj-music",
      redirectLocation
    );
  }

}
