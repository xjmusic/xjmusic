// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.user;

import io.xj.core.resource.ResourceEndpointTest;

//@RunWith(MockitoJUnitRunner.class)
public class UserRecordResourceTest extends ResourceEndpointTest {

  // future test: determine strategy for mocking inner DAO of resources, so these endpoint tests are true unit tests and NOT integration tests

//  @Test
//  public void readOne() {
//    Response response = target().path("users/2").request().get(Response.class);
//    assertEquals(307,response.getStatus());
//    MultivaluedMap<String, Object> headers = response.getHeaders();
//    Object redirectLocation = headers.getFirst("Location");
//    assertEquals(
//      "https://accounts.google.com/o/oauth2/auth" +
//        "?client_id=12345" +
//        "&redirect_uri=https://xj.io/api/69/auth/google/callback" +
//        "&response_type=code" +
//        "&scope=profile%20email" +
//        "&state=xj-music",
//      redirectLocation
//      );
//  }
//
//  @Before
//  public void setUp() throws Exception {
//    // Base
//    System.setProperty("app.url.base","https://xj.io/");
//    System.setProperty("app.url.api", "api/69/");
//    super.before();
//  }
//
//  @After
//  public void tearDown() throws Exception {
//    super.after();
//    System.clearProperty("app.url.base");
//    System.clearProperty("app.url.api");
//  }
//
//  @Override
//  protected String[] packages() {
//    return new String[]{"io.xj.hub"};
//  }

}
