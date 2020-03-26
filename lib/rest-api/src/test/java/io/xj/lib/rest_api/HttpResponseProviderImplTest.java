// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HttpResponseProviderImplTest {
  HttpResponseProvider subject;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new RestApiModule());
    injector.getInstance(ApiUrlProvider.class).setAppBaseUrl("/");
    subject = injector.getInstance(HttpResponseProvider.class);
  }

  @Test
  public void internalRedirect() {
    Response result = subject.internalRedirect("kingdom-come");

    assertEquals(307, result.getStatus());
    assertEquals("/kingdom-come", result.getLocation().getPath());
  }

  @Test
  public void internalRedirectWithCookie() {
    NewCookie cookie = NewCookie.valueOf("Fun=97");
    Response result = subject.internalRedirectWithCookie("kingdom-come", cookie);

    assertEquals("Fun=97;Version=1", result.getCookies().get("Fun").toString());
  }

  @Test
  public void unauthorized() {
    Response result = subject.unauthorized();

    assertEquals(401, result.getStatus());
  }

  @Test
  public void notFound() {
    Response result = subject.notFound(new MockEntity().setId(UUID.randomUUID().toString()));

    assertEquals(404, result.getStatus());
  }

  @Test
  public void failure() {
    Response result = subject.failure(new RestApiException("Fails"));

    assertEquals(400, result.getStatus());
  }

  @Test
  public void failure_andCode() {
    Response result = subject.failure(new RestApiException("Fails"), 422);

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failure_andCode_serverFailureUnknownException() {
    Response result = subject.failure(new Exception("Low Level"), 422);

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failureToCreate() {
    Response result = subject.failureToCreate(new RestApiException("Fails"));

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failureToUpdate() {
    Response result = subject.failureToUpdate(new RestApiException("Fails"));

    assertEquals(422, result.getStatus());
  }

  @Test
  public void notAcceptable() {
    Response result = subject.notAcceptable("at all");

    assertEquals(406, result.getStatus());
  }

  @Test
  public void noContent() {
    Response result = subject.noContent();

    assertEquals(204, result.getStatus());
    assertFalse(result.hasEntity());
  }


}
