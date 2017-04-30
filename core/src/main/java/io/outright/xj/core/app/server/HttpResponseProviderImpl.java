package io.outright.xj.core.app.server;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.transport.JSON;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

public class HttpResponseProviderImpl implements HttpResponseProvider {
  private static Logger log = LoggerFactory.getLogger(HttpResponseProviderImpl.class);
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
    if (e.getClass().equals(BusinessException.class)) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(code)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();
    } else {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }
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

}
