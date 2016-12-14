// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.hub.resources.auth.google;

import io.outright.xj.core.application.resources.ResourceEndpointTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class AuthGoogleResourceTest extends ResourceEndpointTest {
  @Test
  public void GetAuthGoogle() {
    Response response = target().path("auth/google").request().get(Response.class);
    assert response.getStatus()==307;
    MultivaluedMap<String, Object> headers = response.getHeaders();
    assert headers.getFirst("Location")
      .equals("https://accounts.google.com/o/oauth2/auth" +
        "?client_id=12345" +
        "&redirect_uri=http://xj.outright.io/auth/google/callback" +
        "&response_type=code" +
        "&scope=profile" +
        "&state=xj-music"
      );
  }

  @Before
  public void before() throws Exception {
    System.setProperty("auth.google.id","12345");
    System.setProperty("app.url","http://xj.outright.io/");
    super.before();
  }

  @After
  public void after() throws Exception {
    super.after();
    System.clearProperty("auth.google.id");
    System.clearProperty("app.url");
  }

  @Override
  protected String[] packages() {
    return new String[]{"io.outright.xj.hub"};
  }

}
