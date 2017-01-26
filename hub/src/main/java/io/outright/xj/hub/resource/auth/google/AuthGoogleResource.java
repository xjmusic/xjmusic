// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.auth.google;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.external.google.GoogleProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.net.URI;

/**
 * Authenticate via Google OAuth2
 */
@Path("auth/google")
public class AuthGoogleResource {
  private Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(AuthGoogleResource.class);

  /**
   * Begin user OAuth2 authentication via Google.
   *
   * @return Response redirection to auth code request URL
   */
  @GET
  @WebResult
  @PermitAll
  public Response redirectToAuthCodeRequestUrl() {
    GoogleProvider authGoogleProvider = injector.getInstance(GoogleProvider.class);
    String url;
    try {
      url = authGoogleProvider.getAuthCodeRequestUrl();
    } catch (ConfigException e) {
      log.error("Google Auth Provider Failed!", e);
      return Response.serverError().build();
    }

    return Response.temporaryRedirect(URI.create(url)).build();
  }
}
