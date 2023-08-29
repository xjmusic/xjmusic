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
public class LabService {
  Logger LOG = LoggerFactory.getLogger(LabService.class);
  final WebClient webClient;
  final ObjectProperty<LabStatus> status = new SimpleObjectProperty<>(LabStatus.Offline);
  final StringProperty url = new SimpleStringProperty();
  final StringProperty accessToken = new SimpleStringProperty();

  final ObjectProperty<User> authenticatedUser = new SimpleObjectProperty<>();

  public LabService(
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

  void onConnectionChanged() {
    // no op
  }

  void onConnectionSuccess(User user) {
    this.authenticatedUser.set(user);
    this.status.set(LabStatus.Authenticated);
  }

  void onConnectionFailure(Throwable error) {
    LOG.error("Failed to connect to lab!", error);
    this.authenticatedUser.set(null);
    this.status.set(LabStatus.Failed);
  }

  public <T> Mono<T> makeAuthenticatedRequest(String endpoint, HttpMethod method, Class<T> responseType) {
    return webClient.method(method)
      .uri(url.getValue() + endpoint)
      .cookie("access_token", accessToken.getValue())
      .retrieve()
      .bodyToMono(responseType);
  }

  public void disconnect() {
    this.status.set(LabStatus.Offline);
    this.authenticatedUser.set(null);
  }

  public ObjectProperty<LabStatus> statusProperty() {
    return status;
  }

  public StringProperty urlProperty() {
    return url;
  }

  public StringProperty accessTokenProperty() {
    return accessToken;
  }

  public ObjectProperty<User> authenticatedUserProperty() {
    return authenticatedUser;
  }

}
