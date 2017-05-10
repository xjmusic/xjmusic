// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.hub.resource.auth;

import io.outright.xj.core.app.resource.ResourceEndpointTest;

public class AuthResourceTest extends ResourceEndpointTest {

  // TODO write tests for /auth endpoint via HTTP

//  @Test
//  public void GetAuth() {
//    Response response = target().path("auth").request().get(Response.class);
//    assertEquals(307,response.getStatus());
//    MultivaluedMap<String, Object> headers = response.getHeaders();
//    Object redirectLocation = headers.getFirst("Location");
//    assertEquals(
//      "https://accounts.google.com/o/oauth2/auth" +
//        "?client_id=12345" +
//        "&redirect_uri=https://xj.outright.io/auth/google/callback" +
//        "&response_type=code" +
//        "&scope=profile%20email" +
//        "&state=xj-music",
//      redirectLocation
//      );
//  }
//
//  @Before
//  public void before() throws Exception {
//    System.setProperty("auth.google.id","12345");
//    System.setProperty("auth.google.secret","i9hghj");
//    System.setProperty("app.url.base","https://xj.outright.io/");
//    super.before();
//  }
//
//  @After
//  public void after() throws Exception {
//    super.after();
//    System.clearProperty("auth.google.id");
//    System.clearProperty("auth.google.secret");
//    System.clearProperty("app.url.base");
//  }
//
//  @Override
//  protected String[] packages() {
//    return new String[]{"io.outright.xj.hub"};
//  }

}
