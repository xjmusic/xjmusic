// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.transport;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.payload.Payload;
import io.xj.lib.core.payload.PayloadDataType;
import io.xj.lib.core.payload.PayloadError;
import io.xj.lib.core.util.Text;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Objects;

class HttpResponseProviderImpl implements HttpResponseProvider {
  private static final Logger log = LoggerFactory.getLogger(HttpResponseProviderImpl.class);
  private final String appUrl;
  private final GsonProvider gsonProvider;
  private final ApiUrlProvider apiUrlProvider;
  private boolean errorsHaveStacktrace;

  @Inject
  public HttpResponseProviderImpl(
    GsonProvider gsonProvider,
    ApiUrlProvider apiUrlProvider,
    Config config
  ) {
    this.gsonProvider = gsonProvider;

    appUrl = apiUrlProvider.getAppBaseUrl();
    this.apiUrlProvider = apiUrlProvider;
    errorsHaveStacktrace = config.getBoolean("api.errorsHaveStacktrace");
  }

  @Override
  public Response noContent() {
    return Response.noContent().build();
  }

  @Override
  public Response create(Payload payload) {
    return payload.getSelfURI().isPresent() ?
      Response
        .created(payload.getSelfURI().get())
        .entity(gsonProvider.gson().toJson(payload))
        .type(MediaType.APPLICATION_JSON)
        .build() :
      Response
        .created(apiUrlProvider.getApiURI(""))
        .entity(gsonProvider.gson().toJson(payload))
        .type(MediaType.APPLICATION_JSON)
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
  public Response notFound(String resourceType, String resourceId) {
    Payload payload = new Payload()
      .setDataType(PayloadDataType.HasOne)
      .addError(new PayloadError()
        .setCode(String.format("%sNotFound", resourceType))
        .setTitle(String.format("%s not found!", resourceType))
        .setDetail(String.format("Could not find resource type=%s, id=%s", resourceType, resourceId)));

    return Response
      .status(HttpStatus.SC_NOT_FOUND)
      .entity(payload)
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

  @Override
  public Response notFound(Entity resource) {
    return notFound(resource.getResourceType(), resource.getResourceId());
  }

  @Override
  public Response failure(Exception e) {
    return failure(e, HttpStatus.SC_BAD_REQUEST);
  }

  @Override
  public Response failure(Exception e, int code) {
    PayloadError error = PayloadError.of(e);

    Payload payload = new Payload()
      .setDataType(PayloadDataType.HasOne)
      .addError(error);

    if (!Objects.equals(CoreException.class, e.getClass())) {
      log.error(e.getClass().getName(), e);
      if (errorsHaveStacktrace)
        error.setDetail(Text.formatStackTrace(e));
    }

    return Response
      .status(code)
      .entity(gsonProvider.gson().toJson(payload))
      .build();
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
    return failure(new CoreException("Unacceptable entity!"), HttpStatus.SC_NOT_ACCEPTABLE);
  }

  @Override
  public Response ok(Payload payload) {
    return Response
      .ok(gsonProvider.gson().toJson(payload))
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

  @Override
  public Response ok(String content) {
    return Response
      .ok(content)
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

}
