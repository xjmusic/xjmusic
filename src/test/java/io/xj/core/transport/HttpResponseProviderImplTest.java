//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.transport;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.app.AppConfiguration;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.testing.AppTestConfiguration;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HttpResponseProviderImplTest  {
  HttpResponseProvider subject;

  @Before
  public void setUp() {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
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
    Response result = subject.notFound(new Account().setId(UUID.randomUUID()));

    assertEquals(404, result.getStatus());
  }

  @Test
  public void failure() {
    Response result = subject.failure(new CoreException("Fails"));

    assertEquals(400, result.getStatus());
  }

  @Test
  public void failure_andCode() {
    Response result = subject.failure(new CoreException("Fails"), 422);

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failure_andCode_serverFailureUnknownException() {
    Response result = subject.failure(new Exception("Low Level"), 422);

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failureToCreate() {
    Response result = subject.failureToCreate(new CoreException("Fails"));

    assertEquals(422, result.getStatus());
  }

  @Test
  public void failureToUpdate() {
    Response result = subject.failureToUpdate(new CoreException("Fails"));

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
