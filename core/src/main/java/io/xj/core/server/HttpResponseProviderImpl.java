// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.server;

import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.transport.JSON;

import org.jooq.Record;
import org.jooq.Result;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;

public class HttpResponseProviderImpl implements HttpResponseProvider {
  private static final Logger log = LoggerFactory.getLogger(HttpResponseProviderImpl.class);
  private final String appUrl = Config.appBaseUrl();

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
  public Response unauthorizedWithCookie(NewCookie... cookies) {
    return Response
      .status(Response.Status.UNAUTHORIZED)
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
      .entity(JSON.wrapError(entityName + " not found").toString())
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

  @Override
  public Response failure(Exception e) {
    return failure(e, HttpStatus.SC_BAD_REQUEST);
  }

  @Override
  public Response failure(Exception e, int code) {
    if (e.getClass().equals(BusinessException.class))
      return failureBusiness(e, code);
    else
      return failureUnknown(e);
  }

  /**
   Log and return failure response for Unknown Exception

   @param e    exception
   @param code code
   @return response
   */
  private static Response failureBusiness(Exception e, int code) {
    log.warn("failure to do business", e);
    return Response
      .status(code)
      .entity(JSON.wrapError(e.getMessage()).toString())
      .build();
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
      .entity(JSON.wrapError(message).toString())
      .build();
  }

  @Override
  public Response readOne(String keyOne, Record result) {
    if (null != result)
      return Response
        .accepted(JSON.wrap(keyOne, JSON.objectFromRecord(result)).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    else
      return notFound(keyOne);
  }

  @Override
  public Response readOne(String keyOne, Entity result) {
    if (null != result) {
      return Response
        .accepted(JSON.wrap(keyOne, JSON.objectFrom(result)).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else
      return notFound(keyOne);
  }

  @Override
  public <R extends Record> Response readMany(String keyMany, Result<R> results) {
    if (null != results)
      return Response
        .accepted(JSON.wrap(keyMany, JSON.arrayOf(results)).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    else
      return Response.noContent().build();
  }


  @Override
  public <J extends Entity> Response readMany(String keyMany, Collection<J> results) throws Exception {
    if (null != results)
      return Response
        .accepted(JSON.wrap(keyMany, JSON.arrayOf(results)).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    else
      return Response.noContent().build();
  }

  @Override
  public Response create(String keyMany, String keyOne, Record record) {
    if (null != record)
      return Response
        .created(Exposure.apiURI(keyMany + "/" + record.get(Entity.KEY_ID)))
        .entity(JSON.wrap(keyOne, JSON.objectFromRecord(record)).toString())
        .build();

    else
      return failureToCreate(new BusinessException("Could not create " + keyOne));
  }

  @Override
  public Response create(String keyMany, String keyOne, Entity entity) {
    if (null != entity)
      return Response
        .created(Exposure.apiURI(keyMany + "/" + entity.getId()))
        .entity(JSON.wrap(keyOne, JSON.objectFrom(entity)).toString())
        .build();

    else
      return failureToCreate(new BusinessException("Could not create " + keyOne));
  }

}
