// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.resource;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.json.JsonFactory;
import com.google.inject.Injector;
import io.xj.core.access.Access;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.app.AppResource;
import io.xj.core.dao.UserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.external.google.GoogleProvider;
import io.xj.core.model.UserRoleType;
import io.xj.core.transport.ApiUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;

/**
 Current user authentication
 */
@Path("auth")
public class AuthResource extends AppResource {
  private static final Logger log = LoggerFactory.getLogger(AuthResource.class);
  private final JsonFactory jsonFactory;
  private final UserDAO userDAO;
  private final AccessControlProvider accessControlProvider;
  private final GoogleProvider authGoogleProvider;
  private final ApiUrlProvider apiUrlProvider;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public AuthResource(
    Injector injector
  ) {
    super(injector);
    jsonFactory = injector.getInstance(JsonFactory.class);
    userDAO = injector.getInstance(UserDAO.class);
    accessControlProvider = injector.getInstance(AccessControlProvider.class);
    authGoogleProvider = injector.getInstance(GoogleProvider.class);
    apiUrlProvider = injector.getInstance(ApiUrlProvider.class);
  }

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);
    return Response
      .accepted(access.toJSON(jsonFactory))
      .type(MediaType.APPLICATION_JSON)
      .build();
  }


  /**
   Nullify current authentication
   */
  @GET
  @Path("no")
  @PermitAll
  public Response nullifyAuthentication(@Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);

    if (access.isValid()) {
      try {
        userDAO.destroyAllTokens(access.getUserId());
        return response.internalRedirectWithCookie("", accessControlProvider.newExpiredCookie());

      } catch (Exception e) {
        return response.failure(e);
      }

    } else
      return response.internalRedirectWithCookie("", accessControlProvider.newExpiredCookie());
  }

  /**
   Begin user OAuth2 authentication via Google.

   @return Response redirection to auth code request URL
   */
  @GET
  @Path("google")
  @PermitAll
  public Response redirectToAuthCodeRequestUrl() {
    String url;
    try {
      url = authGoogleProvider.getAuthCodeRequestUrl();
    } catch (CoreException e) {
      log.error("Google Auth Provider Failed!", e);
      return Response.serverError().build();
    }

    return Response.temporaryRedirect(URI.create(url)).build();
  }

  /**
   Begin user OAuth2 authentication via Google.

   @return Response temporary redirection to auth URL
   */
  @GET
  @Path("google/callback")
  @PermitAll
  public Response authGoogleCallback(@Context UriInfo ui) {
    AuthorizationCodeResponseUrl authResponse;
    try {
      authResponse = new AuthorizationCodeResponseUrl(ui.getRequestUri().toString());
      if (Objects.nonNull(authResponse.getError())) {
        return errorResponse("Authorization denied: " + authResponse.getErrorDescription());
      }
    } catch (IllegalArgumentException e) {
      return errorResponse("Authorization code response URL missing required parameter(s)");
    } catch (Exception e) {
      return errorResponse("Unknown error while parse authorization code response URL", e);
    }

    String accessToken;
    try {
      accessToken = accessControlProvider.authenticate(authResponse.getCode());
    } catch (CoreException e) {
      return errorResponse("Authentication failed:" + e.getMessage());
    } catch (Exception e) {
      return errorResponse("Unknown error with authenticating access code", e);
    }

    return response.internalRedirectWithCookie(apiUrlProvider.getAppPathWelcome(), accessControlProvider.newCookie(accessToken));
  }

  /**
   Returns a redirect-to-unauthorized-path Response, and logs the error message with an Exception.

   @param msg to log
   @param e   Exception to log
   @return Response
   */
  private Response errorResponse(String msg, Exception e) {
    log.error(msg, e);
    return errorResponse();
  }

  /**
   Returns a redirect-to-unauthorized-path Response, and logs the error message.

   @param msg to log
   @return Response
   */
  private Response errorResponse(String msg) {
    log.error(msg);
    return errorResponse();
  }

  /**
   Returns a redirect-to-unauthorized-path Response.

   @return Response
   */
  private Response errorResponse() {
    return response.internalRedirect(apiUrlProvider.getAppPathUnauthorized());
  }

}
