// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.access;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.*;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
  private JsonapiPayloadFactory payloadFactory;

  @Before
  public void setUp() throws Exception {
    var env = Environment.from(ImmutableMap.of(
      "GOOGLE_CLIENT_ID", "my-google-id",
      "GOOGLE_CLIENT_SECRET", "my-google-secret"
    ));
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new FileStoreModule(), new JsonapiModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
      }
    }));
    payloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    subject = new HubAccessTokenAuthFilter(injector.getInstance(HubAccessControlProvider.class), "access_token");
    subject.setResourceInfo(resourceInfo);
  }

  /**
   [#154580129] User expects to log in without having access to any accounts.
   */
  @Test
  public void filter_allowedWithNoAccounts() throws Exception {
    class TestResource {
      @GET
      @RolesAllowed(HubJsonapiEndpoint.USER)
      public Response get(@Context ContainerRequestContext crc) throws JsonapiException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(payloadFactory.serialize(hubAccess))
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
   [#154580129] User expects to log in without having access to any accounts.
   */
  @Test
  public void filter_allowedPermitAllRoute_withAccessToken() throws Exception {
    class TestResource {
      @GET
      @PermitAll
      public Response get(@Context ContainerRequestContext crc) throws JsonapiException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(payloadFactory.serialize(hubAccess))
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
   [#154580129] User expects to log in without having access to any accounts.
   */
  @Test
  public void filter_nullHubAccessToken() throws Exception {
    class TestResource {
      @GET
      @RolesAllowed(HubJsonapiEndpoint.USER)
      public Response get(@Context ContainerRequestContext crc) throws JsonapiException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(payloadFactory.serialize(hubAccess))
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
   [#154580129] User expects to log in without having access to any accounts.
   */
  @Test
  public void filter_nullHubAccessToken_OkayIfPermitAll() throws Exception {
    class TestResource {
      @GET
      @PermitAll
      public Response get(@Context ContainerRequestContext crc) throws JsonapiException {
        HubAccess hubAccess = HubAccess.fromContext(crc);
        return Response
          .accepted(payloadFactory.serialize(hubAccess))
          .type(MediaType.APPLICATION_JSON)
          .build();
      }
    }
    when(resourceInfo.getResourceMethod())
      .thenReturn(TestResource.class.getMethod("get", ContainerRequestContext.class));
    when(requestContext.getCookies()).thenReturn(ImmutableMap.of(
      "access_token", new Cookie("access_token", "abc-def-0123456789")
    ));
    when(hubAccessControlProvider.get("abc-def-0123456789")).thenThrow(new HubAccessException("Nonexistent"));

    subject.filter(requestContext);

    verify(requestContext, never()).abortWith(any());
  }


}
