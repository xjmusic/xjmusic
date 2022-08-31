// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

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
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Priority(Priorities.AUTHENTICATION)
public class HubAccessTokenAuthFilter implements ContainerRequestFilter {
  private final Logger LOG = LoggerFactory.getLogger(HubAccessTokenAuthFilter.class);
  private final String accessTokenName;
  private final HubAccessControlProvider hubAccessControlProvider;

  /**
   This field is assigned internally by ContainerRequestFilter
   */
  @Context
  protected ResourceInfo resourceInfo; // NOTE This field is assigned internally by ContainerRequestFilter

  @Inject
  public HubAccessTokenAuthFilter(
    HubAccessControlProvider hubAccessControlProvider,
    String accessTokenName
  ) {
    this.hubAccessControlProvider = hubAccessControlProvider;
    this.accessTokenName = accessTokenName;
  }

  public void setResourceInfo(ResourceInfo resourceInfo) {
    this.resourceInfo = resourceInfo;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    String errorMessage = authenticate(requestContext);
    if (Objects.nonNull(errorMessage)) {
      deny(requestContext, new HubAccessException(errorMessage));
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
  private String authenticate(ContainerRequestContext context) {
    // use reflection to get resource method annotation values
    Method method = resourceInfo.getResourceMethod();
    RolesAllowed aRolesAllowed = method.getAnnotation(RolesAllowed.class);
    PermitAll aPermitAll = method.getAnnotation(PermitAll.class);
    DenyAll aDenyAll = method.getAnnotation(DenyAll.class);

    // deny-all is exactly that
    if (Objects.nonNull(aDenyAll))
      return "no hub access permitted";

    // roles required of here on
    if (Objects.isNull(aPermitAll) && Objects.isNull(aRolesAllowed))
      return "resource allows no roles";

    // get AccessControl of (required of here on) hub access token
    Map<String, Cookie> cookies = context.getCookies();
    Cookie accessTokenCookie = cookies.getOrDefault(accessTokenName, null);
    if (Objects.isNull(aPermitAll) && Objects.isNull(accessTokenCookie))
      return "token-less hub access";

    // permit-all is exactly that (but overridden by deny-all)
    // BUT if an hub access token was provided, we're going to treat this as a user auth
    // Required, for example, to implement an idempotent /logout endpoint that redirects somewhere, never returning a 401, whether or not the user is auth'd
    // https://www.pivotaltracker.com/story/show/153110625 Logout, expect redirect to logged-out home view
    if (Objects.nonNull(aPermitAll) && Objects.isNull(accessTokenCookie))
      return null; // allowed

    HubAccess access;
    try {
      access = hubAccessControlProvider.get(accessTokenCookie.getValue());
    } catch (HubAccessException ignored) {
      if (Objects.nonNull(aPermitAll))
        return null; // allowed to supply bad hub access token for a permit-all route
      else
        return "cannot get hub access token";
    }

    if (!access.isValid())
      if (Objects.nonNull(aPermitAll))
        return null; // allowed to have invalid hub access for a permit-all route
      else
        return "invalid hub access token";

    if (!access.isTopLevel() && Objects.isNull(aPermitAll) && !access.isAnyAllowed(aRolesAllowed.value()))
      return "user has no accessible role";

    // set AccessControl in context for use by resource
    access.toContext(context);
    return null; // authenticated
  }

  /**
   Hub access denial implements this central method for logging.

   @param e pertaining to denial.
   */
  private void deny(ContainerRequestContext context, Exception e) {
    LOG.debug("Denied {} /{} ({})", context.getRequest().getMethod(), context.getUriInfo().getPath(), e);
    context.abortWith(
      Response
        .noContent()
        .status(Response.Status.UNAUTHORIZED)
        .build());
  }

}
