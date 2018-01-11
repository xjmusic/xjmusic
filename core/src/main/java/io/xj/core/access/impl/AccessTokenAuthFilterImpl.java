// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access.impl;

import io.xj.core.CoreModule;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import io.xj.core.config.Config;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
  private final AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);

  private final String accessTokenName = Config.accessTokenName();

  /**
   * This field is assigned internally by ContainerRequestFilter
   */
  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    try {
      String errorMessage = authenticate(requestContext);
      if (Objects.nonNull(errorMessage)) {
        deny(requestContext, errorMessage);
      }
    } catch (Exception e) {
      fail(requestContext, e.getMessage());
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
    if (Objects.nonNull(aDenyAll)) {
      return "no access permitted";
    }

    // permit-all is exactly that (but overridden by deny-all)
    if (Objects.nonNull(aPermitAll)) {
      return null; // allowed
    }

    // roles required from here on
    if (Objects.isNull(aRolesAllowed)) {
      return "resource allows no roles";
    }

    // get AccessControl from (required from here on) access token
    Map<String, Cookie> cookies = context.getCookies();
    Cookie accessTokenCookie = cookies.get(accessTokenName);
    if (Objects.isNull(accessTokenCookie)) {
      return "token-less access";
    }

    Access access;
    try {
      access = accessControlProvider.get(accessTokenCookie.getValue());
    } catch (Exception e) {
      log.warn("Could not retrieve access token {}", accessTokenCookie.getValue(), e);
      return "cannot get access token";
    }

    if (!access.isValid()) {
      return "invalid access_token";
    }

    if (!access.isTopLevel() && !access.isAllowed(aRolesAllowed.value())) {
      return "user has no accessible role";
    }

    // setContent AccessControl in context for use by resource
    context.setProperty(Access.CONTEXT_KEY, access);
    return null; // authenticated
  }

  /**
   Access denial implements this central method for logging.

   @param msg pertaining to denial.
   */
  private void deny(ContainerRequestContext context, String msg) {
    log.debug("Denied {} /{} ({})", context.getRequest().getMethod(), context.getUriInfo().getPath(), msg);
    context.abortWith(
      Response
        .noContent()
        .status(Response.Status.UNAUTHORIZED)
        .build()
    );
  }

  /**
   Access failure implements this central method for logging.

   @param msg pertaining to internal server error.
   */
  private void fail(ContainerRequestContext context, String msg) {
    log.error("Failed {} /{} ({})", context.getRequest().getMethod(), context.getUriInfo().getPath(), msg);
    context.abortWith(Response.serverError().build());
  }

}
