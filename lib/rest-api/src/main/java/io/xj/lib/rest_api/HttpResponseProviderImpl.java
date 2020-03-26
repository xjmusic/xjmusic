// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;

@Singleton
class HttpResponseProviderImpl implements HttpResponseProvider {
  private static final Logger log = LoggerFactory.getLogger(HttpResponseProviderImpl.class);
  private final String appUrl;
  private final PayloadFactory payloadFactory;
  private final ApiUrlProvider apiUrlProvider;

  @Inject
  public HttpResponseProviderImpl(
    PayloadFactory payloadFactory,
    ApiUrlProvider apiUrlProvider
  ) {
    this.payloadFactory = payloadFactory;

    appUrl = apiUrlProvider.getAppBaseUrl();
    this.apiUrlProvider = apiUrlProvider;
  }

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  private static String formatStackTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    StackTraceElement[] stack = e.getStackTrace();
    String[] stackLines = Arrays.stream(stack).map(StackTraceElement::toString).toArray(String[]::new);
    return String.join(System.getProperty("line.separator"), stackLines);
  }

  @Override
  public Response noContent() {
    return Response.noContent().build();
  }

  @Override
  public Response create(Payload payload) {
    try {
      return payload.getSelfURI().isPresent() ?
        Response
          .created(payload.getSelfURI().get())
          .entity(payloadFactory.serialize(payload))
          .type(MediaType.APPLICATION_JSON)
          .build() :
        Response
          .created(apiUrlProvider.getApiURI(""))
          .entity(payloadFactory.serialize(payload))
          .type(MediaType.APPLICATION_JSON)
          .build();

    } catch (RestApiException e) {
      log.error("Failed to create {}", payload, e);
      return failureToCreate(e);
    }
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
  public Response notFound(Object resource) {
    try {
      return notFound(PayloadEntity.getResourceType(resource), PayloadEntity.getResourceId(resource));

    } catch (RestApiException e) {
      log.error("Failed to even determine id of {} let alone find it", resource);
      return Response
        .status(HttpStatus.SC_NOT_FOUND)
        .type(MediaType.APPLICATION_JSON)
        .build();
    }
  }

  @Override
  public Response failure(Exception e) {
    return failure(e, HttpStatus.SC_BAD_REQUEST);
  }

  @Override
  public Response failure(Exception e, int code) {
    log.error("Internal server error, code {}", code, e);
    PayloadError error = PayloadError.of(e);

    Payload payload = new Payload()
      .setDataType(PayloadDataType.HasOne)
      .addError(error);

    if (!Objects.equals(RestApiException.class, e.getClass())) {
      log.error(e.getClass().getName(), e);
      error.setDetail(formatStackTrace(e));
    }

    try {
      return Response
        .status(code)
        .entity(payloadFactory.serialize(payload))
        .build();

    } catch (RestApiException e2) {
      log.error("Failed to serialize original failure {} code {}", e, code, e2);
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
    return failure(new RestApiException("Unacceptable entity!"), HttpStatus.SC_NOT_ACCEPTABLE);
  }

  @Override
  public Response ok(Payload payload) {
    try {
      return Response
        .ok(payloadFactory.serialize(payload))
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (RestApiException e) {
      log.error("Failed to serialize payload {}", payload, e);
      return failure(e);
    }
  }

  @Override
  public Response ok(String content) {
    return Response
      .ok(content)
      .type(MediaType.APPLICATION_JSON)
      .build();
  }

}
