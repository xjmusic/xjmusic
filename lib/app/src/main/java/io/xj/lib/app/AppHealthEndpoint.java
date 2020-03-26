// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 Index resource.
 */
@Path("/o2")
@Singleton
public class AppHealthEndpoint {
    @GET
    public String index() {
        return "ok";
    }
}
