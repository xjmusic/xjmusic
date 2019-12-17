// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.access;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.core.CoreModule;
import io.xj.core.access.Access;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import io.xj.core.app.AppConfiguration;
import io.xj.core.model.UserRoleType;
import io.xj.core.testing.AppTestConfiguration;
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
  private AccessTokenAuthFilter subject;
  @Mock
  private AccessControlProvider accessControlProvider;
  @Mock
  private ContainerRequestContext requestContext;
  @Mock
  private ResourceInfo resourceInfo;
  private JsonFactory jsonFactory;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault()
      .withValue("google.clientId", ConfigValueFactory.fromAnyRef("my-google-id"))
      .withValue("google.clientSecret", ConfigValueFactory.fromAnyRef("my-google-secret"));
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(AccessControlProvider.class).toInstance(accessControlProvider);
      }
    })));
    jsonFactory = injector.getInstance(JsonFactory.class);
    subject = injector.getInstance(AccessTokenAuthFilter.class);
    subject.setResourceInfo(resourceInfo);
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
          .accepted(access.toJSON(jsonFactory))
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
      "userId", "61562554-0fd8-11ea-ab87-6f844ba10e4f", // Bill is in no accounts
      "userAuthId", "7c8d0740-0fdb-11ea-b5c9-8f1250fb0100",
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
          .accepted(access.toJSON(jsonFactory))
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
      "userId", "61562554-0fd8-11ea-ab87-6f844ba10e4f", // Bill is in no accounts
      "userAuthId", "7c8d0740-0fdb-11ea-b5c9-8f1250fb0100",
      "roles", "User"
    )));

    subject.filter(requestContext);

    verify(requestContext, never()).abortWith(any());
  }

}
