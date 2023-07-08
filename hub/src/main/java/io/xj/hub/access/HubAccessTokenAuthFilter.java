// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import io.xj.hub.manager.UserManager;
import io.xj.lib.json.ApiUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;


/**
 * See: https://stackoverflow.com/questions/75690685/how-do-i-write-a-spring-boot-request-filter-that-is-able-to-retrieve-the-request/75710266#75710266
 */
@Component
public class HubAccessTokenAuthFilter implements OrderedFilter {
  final Logger LOG = LoggerFactory.getLogger(io.xj.hub.access.HubAccessTokenAuthFilter.class);
  final UserManager userManager;
  final String accessTokenName;
  final RequestMappingHandlerMapping requestMappingHandlerMapping;
  final ApiUrlProvider apiUrlProvider;
  final String apiPathUnauthorized;

  @Autowired
  public HubAccessTokenAuthFilter(
    UserManager userManager,
    RequestMappingHandlerMapping requestMappingHandlerMapping,
    ApiUrlProvider apiUrlProvider,
    @Value("${access.token.name}")
    String accessTokenName,
    @Value("${api.path.unauthorized}")
    String apiPathUnauthorized) {
    this.userManager = userManager;
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    this.apiUrlProvider = apiUrlProvider;
    this.accessTokenName = accessTokenName;
    this.apiPathUnauthorized = apiPathUnauthorized;
  }

  @Override
  public void doFilter(ServletRequest servletReq, ServletResponse servletRes, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) servletReq;
    HttpServletResponse res = (HttpServletResponse) servletRes;

    String errorMessage = authenticate(req);
    if (Objects.nonNull(errorMessage)) {
      deny(req, res, new HubAccessException(errorMessage));
    } else {
      chain.doFilter(req, res);
    }
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
  String authenticate(HttpServletRequest req) {
    try {
      HandlerExecutionChain handlerExeChain = requestMappingHandlerMapping.getHandler(req);
      if (Objects.isNull(handlerExeChain)) {
        return "no resource method";
      }
      HandlerMethod handlerMethod = (HandlerMethod) handlerExeChain.getHandler();
      Method method = handlerMethod.getMethod();

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
      Cookie[] cookies = Objects.nonNull(req.getCookies()) ? req.getCookies() : new Cookie[0];
      Cookie accessTokenCookie = Arrays.stream(cookies).filter(c -> c.getName().equals(accessTokenName)).findFirst().orElse(null);
      if (Objects.isNull(aPermitAll) && Objects.isNull(accessTokenCookie))
        return "token-less hub access";

      // permit-all is exactly that (but overridden by deny-all)
      // BUT if a hub access token was provided, we're going to treat this as a user auth
      // Required, for example, to implement an idempotent /logout endpoint that redirects somewhere, never returning a 401, whether the user is authenticated
      // Log out, expect redirect to logged-out home view https://www.pivotaltracker.com/story/show/153110625
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

    } catch (Exception e) {
      LOG.error("Failure attempting to retrieve access token!", e);
      return String.format("Failure attempting to retrieve access token! %s", e.getMessage());
    }
  }


  /**
   * Hub access denial implements this central method for logging.
   *
   * @param e pertaining to denial.
   */
  void deny(HttpServletRequest req, HttpServletResponse res, Exception e) {
    LOG.debug("Denied {} ({})", req.getRequestURI(), e.getMessage());
    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  /**
   * @return Order defaults to after Spring Session filter
   */
  @Override
  public int getOrder() {
    return REQUEST_WRAPPER_FILTER_MAX_ORDER - 10;
  }
}
