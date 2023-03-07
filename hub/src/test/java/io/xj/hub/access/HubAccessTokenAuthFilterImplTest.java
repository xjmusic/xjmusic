// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.access;

import com.google.common.collect.ImmutableMap;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.UserManager;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.jsonapi.JsonapiException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ResourceInfo;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HubAccessTokenAuthFilterImplTest {
  @Mock
  UserManager userManager;
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  @Mock
  FilterChain chain;
  @Mock
  ResourceInfo resourceInfo;
  //
  private HubAccessTokenAuthFilter subject;

  @RestController
  @RequestMapping("/test")
  static class TestAuthenticatedResource {
    @GetMapping
    @RolesAllowed(HubJsonapiEndpoint.USER)
    public ResponseEntity<HubAccess> get(HttpServletRequest req) throws JsonapiException {
      HubAccess access = HubAccess.fromRequest(req);
      return ResponseEntity
        .accepted()
        .contentType(MediaType.APPLICATION_JSON)
        .body(access);
    }
  }

  @RestController
  @RequestMapping("/test")
  static class TestPublicResource {
    @GetMapping
    @PermitAll
    public ResponseEntity<HubAccess> get(HttpServletRequest req) throws JsonapiException {
      HubAccess access = HubAccess.fromRequest(req);
      return ResponseEntity
        .accepted()
        .contentType(MediaType.APPLICATION_JSON)
        .body(access);
    }
  }

  @Before
  public void setUp() throws Exception {
    var env = AppEnvironment.from(ImmutableMap.of(
      "GOOGLE_CLIENT_ID", "my-google-id",
      "GOOGLE_CLIENT_SECRET", "my-google-secret"
    ));
    subject = new HubAccessTokenAuthFilter(userManager, env);

    when(req.getCookies()).thenReturn(List.of(new Cookie("access_token", "abc-def-0123456789")).toArray(new Cookie[0]));
  }

  /**
   * User expects to log in without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
   */
  @Test
  public void filter_allowedWithNoAccounts() throws Exception {
    var handlerMethod = new HandlerMethod(TestAuthenticatedResource.class, TestAuthenticatedResource.class.getMethod("get", HttpServletRequest.class));
    when(req.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)).thenReturn(handlerMethod);
    when(userManager.get("abc-def-0123456789")).thenReturn(
      HubAccess.create("User")
        .setUserId(UUID.fromString("61562554-0fd8-11ea-ab87-6f844ba10e4f")) // Bill is in no accounts
        .setUserAuthId(UUID.fromString("7c8d0740-0fdb-11ea-b5c9-8f1250fb0100")));

    subject.doFilter(req, res, chain);

    verify(res, never()).sendError(anyInt());
  }

  /**
   * User expects to log in without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
   */
  @Test
  public void filter_allowedPermitAllRoute_withAccessToken() throws Exception {
    var handlerMethod = new HandlerMethod(TestAuthenticatedResource.class, TestAuthenticatedResource.class.getMethod("get", HttpServletRequest.class));
    when(req.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)).thenReturn(handlerMethod);
    when(userManager.get("abc-def-0123456789")).thenReturn(
      HubAccess.create("User")
        .setUserId(UUID.fromString("61562554-0fd8-11ea-ab87-6f844ba10e4f")) // Bill is in no accounts
        .setUserAuthId(UUID.fromString("7c8d0740-0fdb-11ea-b5c9-8f1250fb0100")));

    subject.doFilter(req, res, chain);

    verify(res, never()).sendError(anyInt());
  }

  /**
   * User expects to log in without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
   */
  @Test
  public void filter_nullHubAccessToken() throws Exception {
    var handlerMethod = new HandlerMethod(TestAuthenticatedResource.class, TestAuthenticatedResource.class.getMethod("get", HttpServletRequest.class));
    when(req.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)).thenReturn(handlerMethod);
    when(userManager.get("abc-def-0123456789")).thenThrow(new HubAccessException("Nonexistent"));

    subject.doFilter(req, res, chain);

    verify(res, times(1)).sendError(anyInt());
  }

  /**
   * User expects to log in without having access to any accounts. https://www.pivotaltracker.com/story/show/154580129
   */
  @Test
  public void filter_nullHubAccessToken_OkayIfPermitAll() throws Exception {
    var handlerMethod = new HandlerMethod(TestPublicResource.class, TestPublicResource.class.getMethod("get", HttpServletRequest.class));
    when(req.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)).thenReturn(handlerMethod);
    when(userManager.get("abc-def-0123456789")).thenThrow(new HubAccessException("Nonexistent"));

    subject.doFilter(req, res, chain);

    verify(res, never()).sendError(anyInt());
  }
}
