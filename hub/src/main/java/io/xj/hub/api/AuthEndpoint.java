// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.UserManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
public class AuthEndpoint extends HubJsonapiEndpoint<UserAuth> {
  private static final Logger log = LoggerFactory.getLogger(AuthEndpoint.class);
  private final GoogleProvider authGoogleProvider;
  private final HubAccessControlProvider hubAccessControlProvider;
  private final JsonapiHttpResponseProvider httpResponseProvider;
  private final String appPathUnauthorized;
  private final String appPathWelcome;
  private final UserManager userManager;

  /**
   Constructor
   */
  @Inject
  public AuthEndpoint(
    EntityFactory entityFactory,
    Environment env,
    GoogleProvider authGoogleProvider,
    HubAccessControlProvider hubAccessControlProvider,
    JsonapiHttpResponseProvider httpResponseProvider,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    UserManager userManager
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.authGoogleProvider = authGoogleProvider;
    this.httpResponseProvider = httpResponseProvider;
    this.hubAccessControlProvider = hubAccessControlProvider;
    this.userManager = userManager;

    appPathUnauthorized = env.getApiUnauthorizedRedirectPath();
    appPathWelcome = env.getApiWelcomeRedirectPath();
  }

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) {
    try {
      return Response
        .accepted(payloadFactory.serialize(HubAccess.fromContext(crc)))
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (JsonapiException e) {
      return httpResponseProvider.failure(e);
    }
  }


  /**
   Nullify current authentication
   */
  @GET
  @Path("no")
  @PermitAll
  public Response nullifyAuthentication(@Context ContainerRequestContext crc) {
    HubAccess access = HubAccess.fromContext(crc);

    if (access.isValid()) {
      try {
        userManager.destroyAllTokens(access.getUserId());
        return response.internalRedirectWithCookie("", hubAccessControlProvider.newExpiredCookie());

      } catch (Exception e) {
        return response.failure(e);
      }

    } else
      return response.internalRedirectWithCookie("", hubAccessControlProvider.newExpiredCookie());
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
    } catch (HubAccessException e) {
      log.error("Google Auth Provider Failed!", e);
      return Response.serverError().build();
    }

    return Response.temporaryRedirect(URI.create(url)).build();
  }

  /**
   Begin user OAuth2 authentication via Google.

   @return Response temporary redirection to auth URL
   */
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
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
      accessToken = hubAccessControlProvider.authenticate(authResponse.getCode());
    } catch (ManagerException e) {
      return errorResponse("Authentication failed:" + e.getMessage());
    } catch (Exception e) {
      return errorResponse("Unknown error with authenticating access code", e);
    }

    return response.internalRedirectWithCookie(appPathWelcome, hubAccessControlProvider.newCookie(accessToken));
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
    return response.internalRedirect(appPathUnauthorized);
  }

}
