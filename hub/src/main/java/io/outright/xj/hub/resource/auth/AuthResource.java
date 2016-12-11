package io.outright.xj.hub.resource.auth;

import org.jooq.tools.json.JSONObject;

import javax.jws.WebResult;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Current user authentication
 */
@Path("auth")
public class AuthResource {

  /**
   * Get current authentication.
   *
   * @return JSONObject that will be returned as an application/json response.
   */
  @GET
  @WebResult
  public JSONObject getCurrentAuthentication() {
    throw new ForbiddenException();
  }

  /**
   * Delete currently authenticated session.
   *
   * @return Response with no content, that is used to delete cookies
   */
  @DELETE
  @WebResult
  public Response deleteCurrentAuthentication() {
    return Response.noContent().build();
  }
}
