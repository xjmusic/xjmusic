// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.tables.pojos.User;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class LabServiceImpl implements LabService {
  Logger LOG = LoggerFactory.getLogger(LabServiceImpl.class);
  final WebClient webClient;
  final ObjectProperty<LabStatus> status = new SimpleObjectProperty<>(LabStatus.Offline);
  final StringProperty url = new SimpleStringProperty();
  final StringProperty accessToken = new SimpleStringProperty();

  final ObjectProperty<User> authenticatedUser = new SimpleObjectProperty<>();

  public LabServiceImpl(
    @Value("${lab.base.url}") String defaultLabBaseUrl
  ) {
    this.url.set(defaultLabBaseUrl);
    this.webClient = WebClient.builder().build();
    url.addListener((observable, oldValue, newValue) -> {
      if (Objects.isNull(newValue)) {
        return;
      }
      if (!newValue.endsWith("/")) {
        this.url.set(this.url.getValue() + '/');
      }
      LOG.info("Lab URL changed to: " + this.url.getValue());
    });
  }

  @Override
  public void connect() {
    this.url.set(url.getValue());
    this.accessToken.set(accessToken.getValue());
    this.status.set(LabStatus.Connecting);

    makeAuthenticatedRequest("api/2/users/me", HttpMethod.GET, User.class)
      .subscribe(
        (User user) -> Platform.runLater(() -> this.onConnectionSuccess(user)),
        error -> Platform.runLater(() -> this.onConnectionFailure(error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  @Override
  public void onConnectionChanged() {
    // no op
  }

  @Override
  public void onConnectionSuccess(User user) {
    this.authenticatedUser.set(user);
    this.status.set(LabStatus.Authenticated);
  }

  @Override
  public void onConnectionFailure(Throwable error) {
    LOG.error("Failed to connect to lab!", error);
    this.authenticatedUser.set(null);
    this.status.set(LabStatus.Failed);
  }

  @Override
  public <T> Mono<T> makeAuthenticatedRequest(String endpoint, HttpMethod method, Class<T> responseType) {
    return webClient.method(method)
      .uri(url.getValue() + endpoint)
      .cookie("access_token", accessToken.getValue())
      .retrieve()
      .bodyToMono(responseType);
  }

  @Override
  public void disconnect() {
    this.status.set(LabStatus.Offline);
    this.authenticatedUser.set(null);
  }

  @Override
  public ObjectProperty<LabStatus> statusProperty() {
    return status;
  }

  @Override
  public StringProperty urlProperty() {
    return url;
  }

  @Override
  public StringProperty accessTokenProperty() {
    return accessToken;
  }

  @Override
  public ObjectProperty<User> authenticatedUserProperty() {
    return authenticatedUser;
  }
}
