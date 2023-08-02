// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;


import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Health resource.
 */
@Path("healthz")
public class TestAppHealthEndpoint {

  public TestAppHealthEndpoint() {
  }

  @GET
  @PermitAll
  public String index() {
    return "ok";
  }
}
