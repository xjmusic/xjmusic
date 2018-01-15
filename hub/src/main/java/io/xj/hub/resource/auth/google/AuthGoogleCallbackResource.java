// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.auth.google;

import io.xj.core.CoreModule;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.config.Config;
import io.xj.core.exception.AccessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.transport.HttpResponseProvider;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 Root resource (exposed at "o2" path)
 */
@Path("auth/google/callback")
public class AuthGoogleCallbackResource {
  private static final String redirectPathUnauthorized = Config.appPathUnauthorized();
  private static final String redirectPathSuccess = Config.appPathSuccess();
  private final Logger log = LoggerFactory.getLogger(AuthGoogleCallbackResource.class);
  private final Injector injector = Guice.createInjector(new CoreModule());
  private final AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  /**
   Begin user OAuth2 authentication via Google.

   @return Response temporary redirection to auth URL
   */
  @GET
  @WebResult
  @PermitAll
  public Response authGoogleCallback(@Context UriInfo ui) {
    AuthorizationCodeResponseUrl authResponse;
    try {
      authResponse = new AuthorizationCodeResponseUrl(ui.getRequestUri().toString());
      if (authResponse.getError() != null) {
        return errorResponse("Authorization denied: " + authResponse.getErrorDescription());
      }
    } catch (java.lang.IllegalArgumentException e) {
      return errorResponse("Authorization code response URL missing required parameter(s)");
    } catch (Exception e) {
      return errorResponse("Unknown error while parse authorization code response URL", e);
    }

    String accessToken;
    try {
      accessToken = accessControlProvider.authenticate(authResponse.getCode());
    } catch (AccessException e) {
      return errorResponse("Authentication failed:" + e.getMessage());
    } catch (ConfigException e) {
      return errorResponse("Configuration error" + e.getMessage());
    } catch (DatabaseException e) {
      return errorResponse("Database error" + e.getMessage());
    } catch (Exception e) {
      return errorResponse("Unknown error with authenticating access code", e);
    }

    return response.internalRedirectWithCookie(redirectPathSuccess, accessControlProvider.newCookie(accessToken));
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
    return response.internalRedirect(redirectPathUnauthorized);
  }
}
