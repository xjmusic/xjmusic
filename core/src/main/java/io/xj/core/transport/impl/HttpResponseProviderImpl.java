// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport.impl;

import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.transport.JSON;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
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
      .entity(jsonString(JSON.wrapError(entityName + " not found")))
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
      .entity(jsonString(JSON.wrapError(message)))
      .build();
  }


  @Override
  public Response readOne(String keyOne, Entity entity) {
    return readOne(keyOne, JSON.objectFrom(entity));
  }

  @Override
  public Response readOne(String keyOne, JSONObject obj) {
    if (null != obj) {
      return Response
        .accepted(jsonString(JSON.wrap(keyOne, obj)))
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else
      return notFound(keyOne);
  }


  @Override
  public <J extends Entity> Response readMany(String keyMany, Collection<J> results) throws Exception {
    if (null != results)
      return Response
        .accepted(jsonString(JSON.wrap(keyMany, JSON.arrayOf(results))))
        .type(MediaType.APPLICATION_JSON)
        .build();
    else
      return Response.noContent().build();
  }

  @Override
  public Response create(String keyMany, String keyOne, Entity entity) {
    if (null != entity)
      return Response
        .created(Exposure.apiURI(keyMany + "/" + entity.getId()))
        .entity(jsonString(JSON.wrap(keyOne, JSON.objectFrom(entity))))
        .build();

    else
      return failureToCreate(new BusinessException("Could not create " + keyOne));
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
      .entity(jsonString(JSON.wrapError(e.getMessage())))
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

  /**
   Write a JSON object to a string

   @param obj to write
   @return json object
   */
  private static String jsonString(JSONObject obj) {
    return obj.toString();
  }
}
