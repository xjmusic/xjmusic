// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.access;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubTestConfiguration;
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
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubAccessTokenAuthFilterImplTest {
  @Mock
  private HubAccessControlProvider hubAccessControlProvider;
  @Mock
  private ContainerRequestContext requestContext;
  @Mock
  private ResourceInfo resourceInfo;
  @Mock
  private Request request;
  @Mock
  private UriInfo uriInfo;
  //
  private HubAccessTokenAuthFilter subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault()
      .withValue("google.clientId", ConfigValueFactory.fromAnyRef("my-google-id"))
      .withValue("google.clientSecret", ConfigValueFactory.fromAnyRef("my-google-secret"));
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new FileStoreModule(), new JsonApiModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
      }
    })));
    entityFactory = injector.getInstance(EntityFactory.class);
    subject = new HubAccessTokenAuthFilter(injector.getInstance(HubAccessControlProvider.class), "access_token");
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
      public Response get(@Context ContainerRequestContext crc) throws EntityException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(entityFactory.serialize(hubAccess))
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(requestContext.getRequest()).thenReturn(request);
    when(request.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");
    when(hubAccessControlProvider.get("abc-def-0123456789")).thenReturn(
      HubAccess.create("User")
        .setUserId(UUID.fromString("61562554-0fd8-11ea-ab87-6f844ba10e4f")) // Bill is in no accounts
        .setUserAuthId(UUID.fromString("7c8d0740-0fdb-11ea-b5c9-8f1250fb0100")));

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
      public Response get(@Context ContainerRequestContext crc) throws EntityException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(entityFactory.serialize(hubAccess))
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(hubAccessControlProvider.get("abc-def-0123456789")).thenReturn(
      HubAccess.create("User")
        .setUserId(UUID.fromString("61562554-0fd8-11ea-ab87-6f844ba10e4f")) // Bill is in no accounts
        .setUserAuthId(UUID.fromString("7c8d0740-0fdb-11ea-b5c9-8f1250fb0100")));

    subject.filter(requestContext);

    verify(requestContext, never()).abortWith(any());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void filter_nullHubAccessToken() throws Exception {
    class TestResource {
      @GET
      @RolesAllowed(UserRoleType.USER)
      public Response get(@Context ContainerRequestContext crc) throws EntityException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(entityFactory.serialize(hubAccess))
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(requestContext.getRequest()).thenReturn(request);
    when(request.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");
    when(hubAccessControlProvider.get("abc-def-0123456789")).thenThrow(new HubAccessException("Nonexistent"));

    subject.filter(requestContext);

    verify(requestContext, times(1)).abortWith(any());
  }

  /**
   [#154580129] User expects to login without having access to any accounts.
   */
  @Test
  public void filter_nullHubAccessToken_OkayIfPermitAll() throws Exception {
    class TestResource {
      @GET
      @PermitAll
      public Response get(@Context ContainerRequestContext crc) throws EntityException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(entityFactory.serialize(hubAccess))
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(requestContext.getRequest()).thenReturn(request);
    when(request.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");
    when(hubAccessControlProvider.get("abc-def-0123456789")).thenThrow(new HubAccessException("Nonexistent"));

    subject.filter(requestContext);

    verify(requestContext, never()).abortWith(any());
  }


}
