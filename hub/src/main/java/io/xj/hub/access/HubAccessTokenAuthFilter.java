// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import io.xj.hub.manager.UserManager;
import io.xj.lib.app.AppEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Component
public class HubAccessTokenAuthFilter implements Filter {
  private final Logger LOG = LoggerFactory.getLogger(HubAccessTokenAuthFilter.class);

  private final UserManager userManager;
  private final String accessTokenName;

  @Autowired
  public HubAccessTokenAuthFilter(
    UserManager userManager,
    AppEnvironment env
  ) {
    this.userManager = userManager;
    accessTokenName = env.getAccessTokenName();
  }

  @Override
  public void doFilter(ServletRequest servletReq, ServletResponse servletRes, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) servletReq;
    HttpServletResponse res = (HttpServletResponse) servletRes;

    String errorMessage = authenticate(req);
    if (Objects.nonNull(errorMessage)) {
      deny(req, res, new HubAccessException(errorMessage));
    }

    chain.doFilter(req, res);
  }

  /**
   * Authenticates a request by access token.
   * <p>
   * The result is written to the `context` filter chain
   * No Exception is thrown, during a successful denial
   * after outcome is known, return
   *
   * @param req http servlet request
   * @return null if allowed -- String message on failure
   */
  @Nullable
  private String authenticate(HttpServletRequest req) {
    Method method = getMethod(req);

    if (Objects.isNull(method)) {
      return "no resource method";
    }

    // use reflection to get resource method annotation values
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
    Cookie[] cookies = req.getCookies();
    Cookie accessTokenCookie = Arrays.stream(cookies).filter(c -> c.getName().equals(accessTokenName)).findFirst().orElse(null);
    if (Objects.isNull(aPermitAll) && Objects.isNull(accessTokenCookie))
      return "token-less hub access";

    // permit-all is exactly that (but overridden by deny-all)
    // BUT if an hub access token was provided, we're going to treat this as a user auth
    // Required, for example, to implement an idempotent /logout endpoint that redirects somewhere, never returning a 401, whether or not the user is auth'd
    // Logout, expect redirect to logged-out home view https://www.pivotaltracker.com/story/show/153110625
    if (Objects.nonNull(aPermitAll) && Objects.isNull(accessTokenCookie))
      return null; // allowed

    HubAccess access;
    try {
      access = userManager.get(accessTokenCookie.getValue());
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
    access.authenticate(req);
    return null; // authenticated

  }


  /**
   * Use reflection to get the resource method.
   *
   * @param req (http) servlet request
   * @return resource method
   */
  private Method getMethod(HttpServletRequest req) {
    HandlerMethod handlerMethod = (HandlerMethod) req.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
    if (handlerMethod != null) {
      return handlerMethod.getMethod();
    }
    return null;
  }

  /**
   * Hub access denial implements this central method for logging.
   *
   * @param e pertaining to denial.
   */
  private void deny(HttpServletRequest req, HttpServletResponse res, Exception e) throws IOException {
    LOG.debug("Denied {} ({})", req.getRequestURI(), e.getMessage());
    res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
