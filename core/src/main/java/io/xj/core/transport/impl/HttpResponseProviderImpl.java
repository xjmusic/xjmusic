// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport.impl;

import com.google.inject.Inject;
import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;
import io.xj.core.transport.GsonProvider;
import io.xj.core.transport.HttpResponseProvider;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.Objects;

public class HttpResponseProviderImpl implements HttpResponseProvider {
  private static final Logger log = LoggerFactory.getLogger(HttpResponseProviderImpl.class);
  private final String appUrl = Config.appBaseUrl();
  private final GsonProvider gsonProvider;

  @Inject
  public HttpResponseProviderImpl(
    GsonProvider gsonProvider
  ) {
    this.gsonProvider = gsonProvider;
  }

  /**
   Log and return failure response for Unknown Exception

   @param e exception
   @return response
   */
  private static Response failureUnknown(Exception e) {
    log.error(e.getClass().getName(), e);
    return Response.serverError().build();
  }

  /**
   Log and return failure response for Unknown Exception

   @param e    exception
   @param code code
   @return response
   */
  private Response failureCore(Exception e, int code) {
    return Response
      .status(code)
      .entity(gsonProvider.wrapError(e.getMessage()))
      .build();
  }

  @Override
  public Response internalRedirect(String path) {
    return Response.temporaryRedirect(URI.create(appUrl + path)).build();
  }

  @Override
  public Response internalRedirectWithCookie(String path, NewCookie... cookies) {
    return Response
      .temporaryRedirect(URI.create(appUrl + path))
      .cookie(cookies)
      .build();
  }

  @Override
  public Response unauthorized() {
    return Response
      .status(Response.Status.UNAUTHORIZED)
      .build();
  }

  @Override
  public Response notFound(String entityName) {
    return Response
      .status(HttpStatus.SC_NOT_FOUND)
      .entity(gsonProvider.wrapError(entityName + " not found"))
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

  @Override
  public Response failure(Exception e) {
    return failure(e, HttpStatus.SC_BAD_REQUEST);
  }

  @Override
  public Response failure(Exception e, int code) {
    if (Objects.equals(e.getClass(), CoreException.class))
      return failureCore(e, code);
    else
      return failureUnknown(e);
  }

  @Override
  public Response failureToCreate(Exception e) {
    return failure(e, HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }

  @Override
  public Response failureToUpdate(Exception e) {
    return failure(e, HttpStatus.SC_UNPROCESSABLE_ENTITY);
  }

  @Override
  public Response notAcceptable(String message) {
    return Response
      .status(HttpStatus.SC_NOT_ACCEPTABLE)
      .entity(gsonProvider.wrapError(message))
      .build();
  }

  @Override
  public Response readOne(String keyOne, Object obj) {
    if (Objects.isNull(obj))
      return notFound(keyOne);

    return Response
      .accepted(gsonProvider.wrap(keyOne, obj))
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

  @Override
  public Response readMany(String keyMany, Collection results) {
    return Response
      .accepted(gsonProvider.wrap(keyMany, results))
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

  @Override
  public Response create(String keyMany, String keyOne, Entity entity) {
    if (Objects.isNull(entity))
      return failureToCreate(new CoreException("Could not create " + keyOne));

    return Response
      .created(Exposure.apiURI(keyMany + "/" + entity.getId()))
      .entity(gsonProvider.wrap(keyOne, gsonProvider.gson().toJson(entity)))
      .build();
  }
}
