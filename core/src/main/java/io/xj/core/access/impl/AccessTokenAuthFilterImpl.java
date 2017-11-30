// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.access.impl;

import io.xj.core.CoreModule;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import io.xj.core.config.Config;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Priority(Priorities.AUTHENTICATION)
public class AccessTokenAuthFilterImpl implements AccessTokenAuthFilter {
  private final static Injector injector = Guice.createInjector(new CoreModule());
  private final Logger log = LoggerFactory.getLogger(AccessTokenAuthFilterImpl.class);
  private final AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);

  private final String accessTokenName = Config.accessTokenName();

  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    try {
      authenticate(context);
    } catch (Exception e) {
      failed(context, e.getMessage());
    }
  }

  /**
   Authenticates a request by access token.

   @param context of request.
   @return whether authentication is okay.
   */
  private Boolean authenticate(ContainerRequestContext context) throws Exception {
    // use reflection to get resource method annotation values
    Method method = resourceInfo.getResourceMethod();
    RolesAllowed aRolesAllowed = method.getAnnotation(RolesAllowed.class);
    PermitAll aPermitAll = method.getAnnotation(PermitAll.class);
    DenyAll aDenyAll = method.getAnnotation(DenyAll.class);

    // denied-all is exactly that
    if (aDenyAll != null) {
      return denied(context, "all access");
    }

    // permit-all is exactly that (but overridden by deny-all)
    if (aPermitAll != null) {
      return allowed();
    }

    // roles required from here on
    if (aRolesAllowed == null) {
      return denied(context, "resource allows no roles");
    }

    // get AccessControl from (required from here on) access token
    Map<String, Cookie> cookies = context.getCookies();
    Cookie accessTokenCookie = cookies.get(accessTokenName);
    if (accessTokenCookie == null) {
      return denied(context, "token-less access");
    }

    Access access;
    try {
      access = accessControlProvider.get(accessTokenCookie.getValue());
    } catch (Exception e) {
      return failed(context, "cannot get access token (" + e.getClass().getName() + "): " + e);
    }
    if (!access.valid()) {
      return denied(context, "invalid access_token");
    }

    if (!access.isTopLevel() && !access.matchAnyOf(aRolesAllowed.value())) {
      return denied(context, "user has no accessible role");
    }

    // setContent AccessControl in context for use by resource
    context.setProperty(Access.CONTEXT_KEY, access);
    return allowed();
  }

  /**
   Access denial implements this central method for logging.

   @param msg pertaining to denial.
   @return Boolean
   */
  private Boolean denied(ContainerRequestContext context, String msg) {
    log.debug("Denied " + context.getRequest().getMethod() + " /" + context.getUriInfo().getPath() + " (" + msg + ")");
    context.abortWith(
      Response
        .noContent()
        .status(Response.Status.UNAUTHORIZED)
        .build()
    );
    return false;
  }

  /**
   Access failure implements this central method for logging.

   @param msg pertaining to internal server error.
   @return Boolean
   */
  private Boolean failed(ContainerRequestContext context, String msg) {
    log.error("Failed " + context.getRequest().getMethod() + " /" + context.getUriInfo().getPath() + " (" + msg + ")");
    context.abortWith(Response.serverError().build());
    return false;
  }

  /**
   Allow access.

   @return Boolean
   */
  private Boolean allowed() {
    return true;
  }
}
