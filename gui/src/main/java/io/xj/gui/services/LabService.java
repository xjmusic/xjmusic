package io.xj.gui.services;

import io.xj.gui.controllers.MainWindowController;
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

@Service
public class LabService {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);
  private final WebClient webClient;
  private final ObjectProperty<LabStatus> status = new SimpleObjectProperty<>(LabStatus.Initializing);
  private final StringProperty url = new SimpleStringProperty();
  private final StringProperty accessToken = new SimpleStringProperty();

  public LabService(
    @Value("${lab.base.url}") String defaultLabBaseUrl
  ) {
    this.url.set(defaultLabBaseUrl);
    this.webClient = WebClient.builder().build();
    this.status.set(LabStatus.Ready);
    url.addListener((observable, oldValue, newValue) -> {
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

    makeAuthenticatedRequest("api/2/users/me", HttpMethod.GET, Object.class)
      .subscribe(
        response -> Platform.runLater(() -> this.onConnectionSuccess(response)),
        error -> Platform.runLater(() -> this.onConnectionFailure(error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  private void onConnectionChanged() {
    // todo idk
//    if (this.status.getValue() == LabStatus.Authenticated) {
//      this.status.set(LabStatus.Failed);
//    }
  }

  void onConnectionSuccess(Object response) {
    var hello = 123;// todo: remove this line
    this.status.set(LabStatus.Authenticated);
  }

  void onConnectionFailure(Throwable error) {
    var hello = 123;// todo: remove this line
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
    this.status.set(LabStatus.Disconnected);
    this.url.set(null);
    this.accessToken.set(null);
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
}
