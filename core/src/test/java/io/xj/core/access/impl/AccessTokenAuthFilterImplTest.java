// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.access.impl;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import io.xj.core.model.user.role.UserRoleType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenAuthFilterImplTest {
  private Injector injector;
  private AccessTokenAuthFilter subject;
  @Mock
  private AccessControlProvider accessControlProvider;
  @Mock
  private ContainerRequestContext requestContext;
  @Mock
  private ResourceInfo resourceInfo;

  private static Injector createInjector() {
    return Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
        }
      }));
  }

  @Before
  public void setUp() throws Exception {
    System.setProperty("auth.google.id", "my-google-id");
    System.setProperty("auth.google.secret", "my-google-secret");
    injector = createInjector();
    subject = injector.getInstance(AccessTokenAuthFilter.class);
    subject.setTestResources(resourceInfo, accessControlProvider);
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty("auth.google.secret");
    System.clearProperty("auth.google.id");
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void filter_allowedWithNoAccounts() throws Exception {
    class TestResource {
      @GET
      @RolesAllowed(UserRoleType.USER)
      public Response get(@Context ContainerRequestContext crc) {
        Access access = Access.fromContext(crc);
        return Response
          .accepted(access.toJSON())
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(accessControlProvider.get("abc-def-0123456789")).thenReturn(new Access(ImmutableMap.of(
      "userId", "4", // Bill is in no accounts
      "userAuthId", "12",
      "roles", "User"
    )));

    subject.filter(requestContext);

    verify(requestContext, never()).abortWith(any());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void filter_allowedPermitAllRoute_withAccessToken() throws Exception {
    class TestResource {
      @GET
      @PermitAll
      public Response get(@Context ContainerRequestContext crc) {
        Access access = Access.fromContext(crc);
        return Response
          .accepted(access.toJSON())
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(accessControlProvider.get("abc-def-0123456789")).thenReturn(new Access(ImmutableMap.of(
      "userId", "4", // Bill is in no accounts
      "userAuthId", "12",
      "roles", "User"
    )));

    subject.filter(requestContext);

    verify(requestContext, never()).abortWith(any());
  }

}
