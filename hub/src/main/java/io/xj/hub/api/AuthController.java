// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.UserManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * Current user authentication
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends HubJsonapiEndpoint {
  private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);
  private final ApiUrlProvider apiUrlProvider;
  private final GoogleProvider authGoogleProvider;
  private final UserManager userManager;
  private final String appPathUnauthorized;
  private final String appPathWelcome;

  /**
   * Constructor
   */
  public AuthController(
    ApiUrlProvider apiUrlProvider,
    AppEnvironment env,
    EntityFactory entityFactory,
    GoogleProvider authGoogleProvider,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    UserManager userManager
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.apiUrlProvider = apiUrlProvider;
    this.authGoogleProvider = authGoogleProvider;
    this.userManager = userManager;

    appPathUnauthorized = env.getApiUnauthorizedRedirectPath();
    appPathWelcome = env.getApiWelcomeRedirectPath();
  }

  /**
   * Get current authentication.
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(USER)
  public ResponseEntity<HubAccess> getCurrentAuthentication(HttpServletRequest req, HttpServletResponse res) throws IOException {
    var access = HubAccess.fromRequest(req);
    if (access.isValid()) {
      return ResponseEntity
        .accepted()
        .contentType(MediaType.APPLICATION_JSON)
        .body(access);
    } else {
      res.sendRedirect(apiUrlProvider.getAppUrl(appPathUnauthorized));
      return ResponseEntity.noContent().build();
    }
  }


  /**
   * Nullify current authentication
   */
  @GetMapping("/no")
  @RolesAllowed(USER)
  public void nullifyAuthentication(HttpServletRequest req, HttpServletResponse res) throws IOException {
    HubAccess access = HubAccess.fromRequest(req);

    if (access.isValid()) {
      try {
        userManager.destroyAllTokens(access.getUserId());
        res.addCookie(userManager.newExpiredCookie());

      } catch (Exception e) {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }

    } else
      res.addCookie(userManager.newExpiredCookie());
  }

  /**
   * Begin user OAuth2 authentication via Google.
   */
  @GetMapping("/google")
  @PermitAll
  public void redirectToAuthCodeRequestUrl(HttpServletResponse res) throws IOException {
    String url;
    try {
      url = authGoogleProvider.getAuthCodeRequestUrl();
      res.sendRedirect(url);
    } catch (HubAccessException | IOException e) {
      LOG.error("Google Auth Provider Failed!", e);
      res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * Begin user OAuth2 authentication via Google.
   */
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @GetMapping("/google/callback")
  @PermitAll
  public void authGoogleCallback(HttpServletRequest req, HttpServletResponse res) throws IOException {
    AuthorizationCodeResponseUrl authResponse;
    String accessToken;
    try {
      authResponse = new AuthorizationCodeResponseUrl(apiUrlProvider.getAppUrl(String.format("%s?%s",req.getRequestURI(),req.getQueryString())));
      if (Objects.nonNull(authResponse.getError())) {
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authorization denied: " + authResponse.getErrorDescription());
        return;
      }
      accessToken = userManager.authenticate(authResponse.getCode());
      res.addCookie(userManager.newCookie(accessToken));
      res.sendRedirect(apiUrlProvider.getAppUrl(appPathWelcome));
    } catch (IllegalArgumentException e) {
      logAndSendInternalError(res, "Authorization code response URL missing required parameter(s)!", e);
    } catch (ManagerException e) {
      logAndSendInternalError(res, "Authentication failed!", e);
    } catch (Exception e) {
      logAndSendInternalError(res, "Unknown error with authenticating access code!", e);
    }
  }

  private void logAndSendInternalError(HttpServletResponse res, String message, Exception e) throws IOException {
    res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.format("%s\n%s", message, e.getMessage()));
    LOG.error(message, e);
  }

}
