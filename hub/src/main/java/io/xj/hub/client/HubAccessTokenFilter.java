// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.client;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Priority(Priorities.AUTHENTICATION)
public class HubAccessTokenFilter implements ContainerRequestFilter {
  private final Logger log = LoggerFactory.getLogger(HubAccessTokenFilter.class);
  private final String accessTokenName;
  private final HubClient hubClient;

  /**
   This field is assigned internally by ContainerRequestFilter
   */
  @Context
  protected ResourceInfo resourceInfo; // NOTE This field is assigned internally by ContainerRequestFilter

  @Inject
  public HubAccessTokenFilter(
    HubClient hubClient,
    String accessTokenName
  ) {
    this.hubClient = hubClient;
    this.accessTokenName = accessTokenName;
  }

  public void setResourceInfo(ResourceInfo resourceInfo) {
    this.resourceInfo = resourceInfo;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    try {
      String errorMessage = authenticate(requestContext);
      if (Objects.nonNull(errorMessage)) {
        deny(requestContext, new HubClientException(errorMessage));
      }
    } catch (Exception e) {
      fail(requestContext, e);
    }
  }

  /**
   Authenticates a request by access token.
   <p>
   The result is written to the `context` filter chain
   No Exception is thrown, during a successful denial
   after outcome is known, return

   @param context of request.
   @return null if allowed -- String message on failure
   */
  @Nullable
  private String authenticate(ContainerRequestContext context) throws Exception {
    // use reflection to get resource method annotation values
    Method method = resourceInfo.getResourceMethod();
    RolesAllowed aRolesAllowed = method.getAnnotation(RolesAllowed.class);
    PermitAll aPermitAll = method.getAnnotation(PermitAll.class);
    DenyAll aDenyAll = method.getAnnotation(DenyAll.class);

    // deny-all is exactly that
    if (Objects.nonNull(aDenyAll))
      return "no access permitted";

    // roles required of here on
    if (Objects.isNull(aPermitAll) && Objects.isNull(aRolesAllowed))
      return "resource allows no roles";

    // get AccessControl of (required of here on) access token
    Map<String, Cookie> cookies = context.getCookies();
    Cookie accessTokenCookie = cookies.getOrDefault(accessTokenName, null);
    if (Objects.isNull(aPermitAll) && Objects.isNull(accessTokenCookie))
      return "token-less access";

    // permit-all is exactly that (but overridden by deny-all)
    // BUT if an access token was provided, we're going to treat this as a user auth
    // Required, for example, to implement an idempotent /logout endpoint that redirects somewhere, never returning a 401, whether or not the user is auth'd
    // [#153110625] Logout, expect redirect to logged-out home view
    if (Objects.nonNull(aPermitAll) && Objects.isNull(accessTokenCookie))
      return null; // allowed

    HubClientAccess access;
    try {
      access = hubClient.auth(accessTokenCookie.getValue());
    } catch (Exception e) {
      log.warn("Could not retrieve access token {}", accessTokenCookie.getValue(), e);
      if (Objects.nonNull(aPermitAll))
        return null; // allowed to supply bad access token for a permit-all route
      else
        return "cannot get access token";
    }

    if (!access.isValid())
      if (Objects.nonNull(aPermitAll))
        return null; // allowed to have invalid access for a permit-all route
      else
        return "invalid access token";

    if (!access.isTopLevel() && Objects.isNull(aPermitAll) && !access.isAllowed(aRolesAllowed.value()))
      return "user has no accessible role";

    // set AccessControl in context for use by resource
    access.toContext(context);
    return null; // authenticated
  }

  /**
   HubClientAccess denial implements this central method for logging.

   @param e pertaining to denial.
   */
  private void deny(ContainerRequestContext context, Exception e) {
    log.debug("Denied {} /{} ({})", context.getRequest().getMethod(), context.getUriInfo().getPath(), e);
    context.abortWith(
      Response
        .noContent()
        .status(Response.Status.UNAUTHORIZED)
        .build()
    );
  }

  /**
   HubClientAccess failure implements this central method for logging.

   @param e pertaining to internal server error.
   */
  private void fail(ContainerRequestContext context, Exception e) {
    log.error("Failed {} /{} ({})", context.getRequest().getMethod(), context.getUriInfo().getPath(), e);
    context.abortWith(Response.serverError().build());
  }

}
