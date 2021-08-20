// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.UserDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.HubEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
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
public class AuthEndpoint extends HubEndpoint {
  private static final Logger log = LoggerFactory.getLogger(AuthEndpoint.class);
  private final UserDAO userDAO;
  private final HubAccessControlProvider hubAccessControlProvider;
  private final GoogleProvider authGoogleProvider;
  private final ApiUrlProvider apiUrlProvider;
  private final JsonapiHttpResponseProvider httpResponseProvider;

  /**
   Constructor
   */
  @Inject
  public AuthEndpoint(
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory,
    UserDAO userDAO,
    HubAccessControlProvider hubAccessControlProvider,
    GoogleProvider authGoogleProvider,
    ApiUrlProvider apiUrlProvider,
    JsonapiHttpResponseProvider httpResponseProvider
  ) {
    super(response, config, payloadFactory);
    this.userDAO = userDAO;
    this.hubAccessControlProvider = hubAccessControlProvider;
    this.authGoogleProvider = authGoogleProvider;
    this.apiUrlProvider = apiUrlProvider;
    this.httpResponseProvider = httpResponseProvider;
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
    HubAccess hubAccess = HubAccess.fromContext(crc);

    if (hubAccess.isValid()) {
      try {
        userDAO.destroyAllTokens(hubAccess.getUserId());
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
    } catch (DAOException e) {
      return errorResponse("Authentication failed:" + e.getMessage());
    } catch (Exception e) {
      return errorResponse("Unknown error with authenticating access code", e);
    }

    return response.internalRedirectWithCookie(apiUrlProvider.getAppPathWelcome(), hubAccessControlProvider.newCookie(accessToken));
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
