package io.outright.xj.core.app.resource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Root resource (exposed at "o2" path)
 */
@Path("o2")
public class HealthcheckResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Context
    @PermitAll
    public Response healthcheck() {
      return Response
        .accepted()
        .status(Response.Status.OK)
        .build();
    }
}
