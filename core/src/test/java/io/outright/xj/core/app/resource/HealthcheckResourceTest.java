// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.app.resource;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertTrue;

/**
 THIS CLASS IS NOT ACTUALLY IN USE, UNTIL WE IMPLEMENT [#225] Jersey JAX-RS resource tests mock external dependencies, in order to ensure specific conditions.
 */
public class HealthcheckResourceTest extends ResourceEndpointTest {

/*
  @Test
  public void GetHealthcheck() {
    Response response = target().path("o2").request().get(Response.class);
    assertTrue(500 == response.getStatus() || 200 == response.getStatus());
  }
*/

}
