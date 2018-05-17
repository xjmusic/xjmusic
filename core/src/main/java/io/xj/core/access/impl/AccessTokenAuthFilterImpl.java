// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access.impl;

import io.xj.core.CoreModule;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import io.xj.core.config.Config;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Priority(Priorities.AUTHENTICATION)
public class AccessTokenAuthFilterImpl implements AccessTokenAuthFilter {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final Logger log = LoggerFactory.getLogger(AccessTokenAuthFilterImpl.class);
  private AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);

  private final String accessTokenName = Config.accessTokenName();

  /**
   This field is assigned internally by ContainerRequestFilter
   */
  @Context
  private ResourceInfo resourceInfo; // NOTE This field is assigned internally by ContainerRequestFilter

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    try {
      String errorMessage = authenticate(requestContext);
      if (Objects.nonNull(errorMessage)) {
        deny(requestContext, new BusinessException(errorMessage));
      }
    } catch (Exception e) {
      fail(requestContext, e);
    }
  }

  /**
   Override resource info-- FOR TESTING PURPOSES ONLY, in order to mock a resource

   @param resourceInfo to set
   */
  @Override
  public void setTestResources(ResourceInfo resourceInfo, AccessControlProvider accessControlProvider) {
    this.resourceInfo = resourceInfo;
    this.accessControlProvider = accessControlProvider;
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

    // roles required from here on
    if (Objects.isNull(aPermitAll) && Objects.isNull(aRolesAllowed))
      return "resource allows no roles";

    // get AccessControl from (required from here on) access token
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

    Access access;
    try {
      access = accessControlProvider.get(accessTokenCookie.getValue());
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

    // setContent AccessControl in context for use by resource
    access.toContext(context);
    return null; // authenticated
  }

  /**
   Access denial implements this central method for logging.
   * @param e pertaining to denial.

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
   Access failure implements this central method for logging.

   @param e pertaining to internal server error.
   */
  private void fail(ContainerRequestContext context, Exception e) {
    log.error("Failed {} /{} ({})", context.getRequest().getMethod(), context.getUriInfo().getPath(), e);
    context.abortWith(Response.serverError().build());
  }

}
