package io.outright.xj.hub.resource.auth.google;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.ResponseWrapper;
import java.net.URI;

/**
 * Root resource (exposed at "o2" path)
 */
@Path("auth/google")
public class AuthGoogleResource {

    /**
     * Begin user OAuth2 authentication via Google.
     *
     * @return Response temporary redirection to auth URL
     */
    @GET
    @WebResult
    public Response authGoogleBegin() {
      URI uri = URI.create("https://www.google.com");
      return Response.temporaryRedirect(uri).build();
      // TODO implement GET /auth/google
    }
}
