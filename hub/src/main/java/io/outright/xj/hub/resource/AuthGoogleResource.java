package io.outright.xj.hub.resource;

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
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @WebResult
    public String authGoogleBegin() {
      throw new RedirectionException(Response.Status.TEMPORARY_REDIRECT, URI.create("https://www.google.com"));
    }
}
