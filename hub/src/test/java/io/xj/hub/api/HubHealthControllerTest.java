// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.service.PreviewNexusAdmin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HubHealthControllerTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @SuppressWarnings("unused")
  @MockBean
  private PreviewNexusAdmin previewNexusAdmin;

  @Test
  public void healthCheckShouldReturn200OK() {
    when(previewNexusAdmin.isReady()).thenReturn(true);

    ResponseEntity<String> response = restTemplate.getForEntity(
      "http://localhost:" + port + "/healthz", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void failsWithoutServices() {
    when(previewNexusAdmin.isReady()).thenReturn(false);

    ResponseEntity<String> response = restTemplate.getForEntity(
      "http://localhost:" + port + "/healthz", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}

