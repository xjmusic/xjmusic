package io.outright.xj.hub.resources.auth.google;

import io.outright.xj.core.app.AppModule;
import io.outright.xj.core.app.access.UserAccessProvider;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.external.google.GoogleModule;
import io.outright.xj.hub.controller.auth.GoogleAuthController;

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
 * Root resource (exposed at "o2" path)
 */
@Path("auth/google/callback")
public class AuthGoogleCallbackResource {
  private final Logger log = LoggerFactory.getLogger(AuthGoogleCallbackResource.class);
  private final Injector injector = Guice.createInjector(new AppModule(), new GoogleModule());
  private final GoogleAuthController googleAuthController = injector.getInstance(GoogleAuthController.class);
  private final UserAccessProvider userAccessProvider = injector.getInstance(UserAccessProvider.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  private static final String redirectPathUnauthorized = Config.appPathUnauthorized();
  private static final String redirectPathSuccess = Config.appPathSuccess();

  /**
   * Begin user OAuth2 authentication via Google.
   *
   * @return Response temporary redirection to auth URL
   */
  @GET
  @WebResult
  @PermitAll
  public Response authGoogleCallback(@Context UriInfo ui) {
    AuthorizationCodeResponseUrl authResponse;
    authResponse = new AuthorizationCodeResponseUrl(ui.getRequestUri().toString());
    if (authResponse.getError() != null) {
      log.error("Authorization denied: {}", authResponse.getErrorDescription());
      return httpResponseProvider.temporaryRedirect(redirectPathUnauthorized);
    }

    String accessToken;
    try {
      accessToken = googleAuthController.authenticate(authResponse.getCode());
    } catch (AccessException e) {
      log.error("Authentication failed", e);
      return httpResponseProvider.temporaryRedirect(redirectPathUnauthorized);
    } catch (ConfigException e) {
      log.error("Configuration error", e);
      return httpResponseProvider.temporaryRedirect(redirectPathUnauthorized);
    } catch (DatabaseException e) {
      log.error("Database error", e);
      return httpResponseProvider.temporaryRedirect(redirectPathUnauthorized);
    }

    return httpResponseProvider.temporaryRedirectWithCookie(redirectPathSuccess, userAccessProvider.newCookie(accessToken));
  }

}
