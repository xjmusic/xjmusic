package io.outright.xj.core.app.access;

import io.outright.xj.core.app.AppModule;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.AccessException;

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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

@Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
public class AccessTokenAuthFilterImpl implements AccessTokenAuthFilter {
  private final Logger log = LoggerFactory.getLogger(AccessTokenAuthFilterImpl.class);
  private final static Injector injector = Guice.createInjector(new AppModule());
  private final UserAccessProvider userAccessProvider = injector.getInstance(UserAccessProvider.class);

  private final String accessTokenName = Config.accessTokenName();

  @Context
  private ResourceInfo resourceInfo;

  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    // use reflection to get resource method annotation values
    Method method = resourceInfo.getResourceMethod();
    RolesAllowed aRolesAllowed = method.getAnnotation(RolesAllowed.class);
    PermitAll aPermitAll = method.getAnnotation(PermitAll.class);
    DenyAll aDenyAll = method.getAnnotation(DenyAll.class);

    // deny-all is exactly that
    if (aDenyAll != null) { throw new IOException("All access denied."); }

    // permit-all is (unless deny-less) exactly that
    if (aPermitAll != null) { return; }

    // roles required from here on
    if (aRolesAllowed == null) { throw new IOException("No roles allowed; access denied."); }

    // get UserAccess from (required from here on) access token; throw exceptions.
    UserAccess userAccess = userAccess(context);
    if (!userAccess.matchRoles(aRolesAllowed.value())) {
      throw new IOException("User has no accessible role; access denied.");
    }

    // set UserAccess in context for use by resource
    context.setProperty(UserAccess.CONTEXT_KEY,userAccess);
  }

  /**
   * Get user access from the access token cookie in a container request context.
   *
   * @param context from which to get the access token cookie.
   * @return user access for that token.
   * @throws IOException if user access is denied.
   */
  private UserAccess userAccess(ContainerRequestContext context) throws IOException {
    Map<String, Cookie> cookies = context.getCookies();
    Cookie accessTokenCookie = cookies.get(accessTokenName);
    if (accessTokenCookie == null) {
      throw new IOException("Token-less access denied.");
    }
    try {
      return userAccessProvider.get(accessTokenCookie.getValue());
    } catch (AccessException e) {
      log.warn("Invalid access_token!", e);
      throw new IOException("Invalid access_token: "+e.toString());
    }
  }

}
