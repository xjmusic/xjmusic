// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.jsonapi;

import io.xj.lib.Widget;
import io.xj.lib.json.ApiUrlProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class JsonapiResponseProviderImplTest {
  JsonapiResponseProvider subject;

  @BeforeEach
  public void setUp() {
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider("");
    subject = new JsonapiResponseProviderImpl(apiUrlProvider);
  }

  @Test
  public void unauthorized() {
    var result = subject.unauthorized();

    assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
  }

  @Test
  public void notFound() {
    var result = subject.notFound(new Widget().setId(UUID.randomUUID()));

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  public void failure() {
    var result = subject.failure(new JsonapiException("Fails"));

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void failure_andCode() {
    var result = subject.failure(HttpStatus.NOT_ACCEPTABLE, new JsonapiException("Fails"));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
  }

  @Test
  public void failure_andCode_serverFailureUnknownException() {
    var result = subject.failure(HttpStatus.NOT_ACCEPTABLE, new Exception("Low Level"));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
  }

  @Test
  public void failureToCreate() {
    var result = subject.notAcceptable(new JsonapiException("Fails"));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
  }

  @Test
  public void failureToUpdate() {
    var result = subject.notAcceptable(new JsonapiException("Fails"));

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
  }

  @Test
  public void notAcceptable() {
    var result = subject.notAcceptable("at all");

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
  }

  /**
   * 406 not-acceptable errors surface underlying causes https://www.pivotaltracker.com/story/show/175985762
   */
  @Test
  public void notAcceptable_surfacesUnderlyingCauses() {
    var d = new IOException("I am the real cause");
    var e = new JsonapiException("I am the outer cause", d);
    var result = subject.notAcceptable(e);

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
    assertEquals("I am the real cause", Objects.requireNonNull(result.getBody()).getErrors().stream().findFirst().orElseThrow().getTitle());
  }

  @Test
  public void noContent() {
    var result = subject.noContent();

    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
    assertFalse(result.hasBody());
  }
}
