// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import io.xj.core.model.entity.Entity;

import org.json.JSONObject;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Collection;

public interface HttpResponseProvider {
  /**
   Respond with a temporary redirect.

   @param path of redirection target.
   @return response.
   */
  Response internalRedirect(String path);

  /**
   Respond with a set-cookie in addition to a temporary redirect.

   @param path    of redirection target.
   @param cookies to set before redirection.
   @return response.
   */
  Response internalRedirectWithCookie(String path, NewCookie... cookies);

  /**
   Response with unauthorized

   @return response.
   */
  Response unauthorized();

  /**
   Response with entity (named) not found

   @param entityName not found
   @return response
   */
  Response notFound(String entityName);

  /**
   Return a response from an exception

   @param e Exception
   @return Response
   */
  Response failure(Exception e);

  /**
   Return a response from an exception

   @param e    Exception
   @param code HTTP Status Code
   @return Response
   */
  Response failure(Exception e, int code);

  /**
   Return a response from an exception during a Create operation

   @param e Exception
   @return Response
   */
  Response failureToCreate(Exception e);

  /**
   Return a response from an exception during an Update operation

   @param e Exception
   @return Response
   */
  Response failureToUpdate(Exception e);

  /**
   Return a response that the request is not acceptable, with a JSON error payload

   @param message in error payload
   @return response
   */
  Response notAcceptable(String message);

  /**
   Return a response that one record has been read, else an error

   @param keyOne key for one record
   @param entity of record that was read, or null if a 404 ought to be returned instead
   @return response
   */
  Response readOne(String keyOne, Entity entity);

  /**
   Return a response that one JSON object has been read, else an error

   @param keyOne key for one record
   @param obj    of JSON object that was read, or null if a 404 ought to be returned instead
   @return response
   */
  Response readOne(String keyOne, JSONObject obj);

  /**
   Return a response that many records have been read, else an error

   @param keyMany key for many records
   @param results of records that were read, or null if a 404 ought to be returned instead
   @return response
   */
  <J extends Entity> Response readMany(String keyMany, Collection<J> results) throws Exception;

  /**
   Return a response that the request has been created, else an error

   @param keyMany key for many records (within which the newly-created-entity-path, including id, will be calculated)
   @param keyOne  key for one record
   @param entity  the record that was created, or null if none was (and an error ought to be returned)
   @return response
   */
  Response create(String keyMany, String keyOne, Entity entity);
}
