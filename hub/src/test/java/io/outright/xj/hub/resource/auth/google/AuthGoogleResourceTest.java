// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.hub.resource.auth.google;

import io.outright.xj.core.app.resource.ResourceEndpointTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class AuthGoogleResourceTest extends ResourceEndpointTest {
  @Test
  public void GetAuthGoogle() {
    Response response = target().path("auth/google").request().get(Response.class);
    assertEquals(307, response.getStatus());
    MultivaluedMap<String, Object> headers = response.getHeaders();
    Object redirectLocation = headers.getFirst("Location");
    assertEquals(
      "https://accounts.google.com/o/oauth2/auth" +
        "?client_id=12345" +
        "&redirect_uri=https://xj.outright.io/api/69/auth/google/callback" +
        "&response_type=code" +
        "&scope=profile%20email" +
        "&state=xj-music",
      redirectLocation
    );
  }

  @Before
  public void before() throws Exception {
    System.setProperty("auth.google.id", "12345");
    System.setProperty("auth.google.secret", "i9hghj");
    System.setProperty("app.url.base", "https://xj.outright.io/");
    System.setProperty("app.url.api", "api/69/");
    super.before();
  }

  @After
  public void after() throws Exception {
    super.after();
    System.clearProperty("auth.google.id");
    System.clearProperty("auth.google.secret");
    System.clearProperty("app.url.base");
    System.clearProperty("app.url.api");
  }

  @Override
  protected String[] packages() {
    return new String[]{"io.outright.xj.hub"};
  }

}
