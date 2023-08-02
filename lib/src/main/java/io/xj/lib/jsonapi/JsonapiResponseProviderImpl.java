// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.json.ApiUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.xj.lib.util.StringUtils.formatStackTrace;

@Service
public class JsonapiResponseProviderImpl implements JsonapiResponseProvider {
  static final Logger log = LoggerFactory.getLogger(JsonapiResponseProviderImpl.class);
  final String appUrl;
  final ApiUrlProvider apiUrlProvider;

  @Autowired
  public JsonapiResponseProviderImpl(ApiUrlProvider apiUrlProvider) {
    appUrl = apiUrlProvider.getAppBaseUrl();
    this.apiUrlProvider = apiUrlProvider;
    log.debug("HTTP Responses will have Base URL {}", appUrl);
  }

  @Override
  public ResponseEntity<String> noContent() {
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<JsonapiPayload> deletedOk() {
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload) {
    return jsonapiPayload.getSelfURI().isPresent() ? ResponseEntity.created(jsonapiPayload.getSelfURI().orElseThrow()).contentType(MediaType.APPLICATION_JSON).body(jsonapiPayload) : ResponseEntity.created(apiUrlProvider.getAppURI("")).contentType(MediaType.APPLICATION_JSON).body(jsonapiPayload);

  }

  @Override
  public ResponseEntity<JsonapiPayload> unauthorized() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @Override
  public ResponseEntity<JsonapiPayload> unauthorized(Class<?> type, Object identifier, Throwable cause) {
    JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.One).addError(new PayloadError().setCode(String.format("%s Unauthorized", type.getSimpleName())).setTitle(String.format("Not authorized for %s[%s]!", type.getSimpleName(), identifier)).setDetail(cause.getMessage()));

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON).body(jsonapiPayload);
  }

  @Override
  public ResponseEntity<JsonapiPayload> notFound(String type, Object identifier) {
    JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.One).addError(new PayloadError().setCode(String.format("%sNotFound", type)).setTitle(String.format("%s not found!", type)).setDetail(String.format("Could not find resource type=%s, id=%s", type, identifier)));

    return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(jsonapiPayload);
  }

  @Override
  public ResponseEntity<JsonapiPayload> notFound(Class<?> type, Object identifier) {
    return notFound(type.getSimpleName(), identifier);
  }

  @Override
  public ResponseEntity<JsonapiPayload> notFound(Object resource) {
    try {
      return notFound(Entities.getType(resource), Entities.getId(resource));

    } catch (EntityException e) {
      log.error("Failed to even determine id of {} let alone find it", resource);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).build();
    }
  }

  @Override
  public ResponseEntity<String> failureText(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(formatStackTrace(e));
  }

  @Override
  public ResponseEntity<JsonapiPayload> failure(Exception e) {
    return failure(HttpStatus.BAD_REQUEST, e);
  }

  public ResponseEntity<JsonapiPayload> failure(HttpStatus status, Exception e) {
    return failure(status, PayloadError.of(e));
  }

  public ResponseEntity<JsonapiPayload> failure(HttpStatus status, String message) {
    return failure(status, new PayloadError().setCode(String.valueOf(status)).setTitle(message));
  }

  public ResponseEntity<JsonapiPayload> failure(HttpStatus status, PayloadError error) {
    JsonapiPayload payload = new JsonapiPayload().setDataType(PayloadDataType.One).addError(error);

    return ResponseEntity.status(status).body(payload);
  }

  @Override
  public ResponseEntity<JsonapiPayload> notAcceptable(Exception e) {
    if (Objects.nonNull(e.getCause()) && !e.getCause().equals(e))
      return notAcceptable(String.format("%s", e.getCause().getMessage()));
    return notAcceptable(e.getMessage());
  }

  @Override
  public ResponseEntity<JsonapiPayload> notAcceptable(String message) {
    return failure(HttpStatus.NOT_ACCEPTABLE, message);
  }

  @Override
  public ResponseEntity<JsonapiPayload> ok(JsonapiPayload jsonapiPayload) {
    return ResponseEntity.ok(jsonapiPayload);
  }

  @Override
  public ResponseEntity<String> ok(String content) {
    return ResponseEntity.ok(content);
  }

}
