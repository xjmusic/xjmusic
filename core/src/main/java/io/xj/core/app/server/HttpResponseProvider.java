// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.server;

import org.jooq.Record;
import org.jooq.Result;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;

public interface HttpResponseProvider {
  /**
   Respond with a temporary redirect.

   @param path of redirection target.
   @return response.
   */
  Response internalRedirect(String path);

  /**
   Respond with a setContent-cookie in addition to a temporary redirect.

   @param path    of redirection target.
   @param cookies to setContent before redirection.
   @return response.
   */
  Response internalRedirectWithCookie(String path, NewCookie... cookies);

  /**
   Respond with a setContent-cookie in addition to unauthorized

   @param cookies to setContent before unauthorized.
   @return response.
   */
  Response unauthorizedWithCookie(NewCookie... cookies);

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
   @param result of record that was read, or null if a 404 ought to be returned instead
   @return response
   */
  Response readOne(String keyOne, Record result);

  /**
   Return a response that many records have been read, else an error

   @param keyMany key for one record
   @param results of records that were read, or null if a 404 ought to be returned instead
   @return response
   */
  <R extends Record> Response readMany(String keyMany, Result<R> results);

  /**
   Return a response that the request has been created, else an error

   @param keyMany key for many records (within which the newly-created-entity-path, including id, will be calculated)
   @param keyOne  key for one record
   @param record  the record that was created, or null if none was (and an error ought to be returned)
   @return response
   */
  Response create(String keyMany, String keyOne, Record record);
}
