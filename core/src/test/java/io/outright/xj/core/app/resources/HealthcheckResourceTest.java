// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.resources;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class HealthcheckResourceTest extends ResourceEndpointTest {

  @Test
  public void GetHealthcheck() {
    Response response = target().path("o2").request().get(Response.class);
    assertEquals(200, response.getStatus());
  }

}
