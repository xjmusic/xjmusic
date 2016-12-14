package io.outright.xj.hub.resources.auth.google;

import io.outright.xj.core.application.auth.google.GoogleAuthModule;
import io.outright.xj.core.application.auth.google.GoogleAuthProvider;
import io.outright.xj.core.application.exception.AuthenticationException;
import io.outright.xj.core.application.exception.ConfigException;
import io.outright.xj.core.application.server.BaseUrlProvider;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Root resource (exposed at "o2" path)
 */
@Path("auth/google/callback")
public class AuthGoogleCallbackResource {
  private Injector injector = Guice.createInjector(new GoogleAuthModule());
  private Logger log = LoggerFactory.getLogger(AuthGoogleCallbackResource.class);
  private BaseUrlProvider baseUrlProvider = injector.getInstance(BaseUrlProvider.class);
  private GoogleAuthProvider googleAuthProvider = injector.getInstance(GoogleAuthProvider.class);

  /**
   * Begin user OAuth2 authentication via Google.
   *
   * @return Response temporary redirection to auth URL
   */
  @GET
  @WebResult
  public Response authGoogleCallback(@Context UriInfo ui) {

    AuthorizationCodeResponseUrl authResponse =
      new AuthorizationCodeResponseUrl(ui.getRequestUri().toString());
    // check for user-denied error
    if (authResponse.getError() != null) {
      log.error("Authorization denied: {}", authResponse.getErrorDescription());
      return unauthorizedRedirectResponse();
    }

    String token;
    try {
      token = googleAuthProvider.getTokenFromCode(authResponse.getCode());
    } catch (AuthenticationException e) {
      log.error("Authentication failed.", e.getMessage());
      return unauthorizedRedirectResponse();
    }

    log.info("SUCCESS! Access token: " + token);
    log.info("Also state: {}", authResponse.getState()); // TODO remove
    return successRedirectResponse();

    // TODO: get access_token from google
    // TODO: retrieve/create X.J. user
    // TODO: create authorized session
    // TODO: cookie user with session authorization token
  }

  private Response successRedirectResponse() {
    try {
      return Response.temporaryRedirect(URI.create(baseUrlProvider.getUrl()+"welcome")).build();
    } catch (ConfigException e) {
      log.error("Failed to construct response!", e);
      return Response.serverError().build();
    }
  }

  private Response errorRedirectResponse() {
    try {
      return Response.temporaryRedirect(URI.create(baseUrlProvider.getUrl()+"error")).build();
    } catch (ConfigException e) {
      log.error("Failed to construct response!", e);
      return Response.serverError().build();
    }
  }

  private Response unauthorizedRedirectResponse() {
    try {
      return Response.temporaryRedirect(URI.create(baseUrlProvider.getUrl()+"unauthorized")).build();
    } catch (ConfigException e) {
      log.error("Failed to construct response!", e);
      return Response.serverError().build();
    }
  }

}
