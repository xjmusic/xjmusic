// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.xj.hub.HubConfiguration;
import io.xj.hub.tables.pojos.User;
import javafx.application.HostServices;
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

import java.net.URI;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class LabServiceImpl implements LabService {
  private final HostServices hostServices;
  Logger LOG = LoggerFactory.getLogger(LabServiceImpl.class);
  final WebClient webClient;
  final ObjectProperty<LabStatus> status = new SimpleObjectProperty<>(LabStatus.Offline);
  static final Pattern rgxStripLeadingSlash = Pattern.compile("^/");
  final StringProperty baseUrl = new SimpleStringProperty();
  final StringProperty accessToken = new SimpleStringProperty();

  final ObjectProperty<User> authenticatedUser = new SimpleObjectProperty<>();

  final ObjectProperty<HubConfiguration> hubConfig = new SimpleObjectProperty<>();

  public LabServiceImpl(
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") HostServices hostServices,
    @Value("${lab.base.url}") String defaultLabBaseUrl,
    @Value("${audio.base.url}") String audioBaseUrl,
    @Value("${ship.base.url}") String shipBaseUrl,
    @Value("${stream.base.url}") String streamBaseUrl
  ) {
    this.hostServices = hostServices;
    this.baseUrl.set(defaultLabBaseUrl);
    this.webClient = WebClient.builder().build();
    baseUrl.addListener((ignored1, ignored2, value) -> {
      if (Objects.isNull(value)) {
        return;
      }
      if (!value.endsWith("/")) {
        this.baseUrl.set(this.baseUrl.getValue() + '/');
      }
      LOG.info("Lab URL changed to: " + this.baseUrl.getValue());
    });

    this.hubConfig.set(new HubConfiguration()
      .setApiBaseUrl(defaultLabBaseUrl)
      .setAudioBaseUrl(audioBaseUrl)
      .setBaseUrl(defaultLabBaseUrl)
      .setShipBaseUrl(shipBaseUrl)
      .setStreamBaseUrl(streamBaseUrl));
    this.hubConfig.addListener((ignored1, ignored2, value) ->
      LOG.info("Lab configured: " +
        "Base: " + value.getBaseUrl() + ", " +
        "API: " + value.getApiBaseUrl() + ", " +
        "Audio: " + value.getAudioBaseUrl() + ", " +
        "Ship: " + value.getShipBaseUrl() + ", " +
        "Stream: " + value.getStreamBaseUrl() + ", "));
  }

  @Override
  public void connect() {
    this.baseUrl.set(baseUrl.getValue());
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
    this.status.set(LabStatus.Configuring);
    makeAuthenticatedRequest("api/2/config", HttpMethod.GET, HubConfiguration.class)
      .subscribe(
        (HubConfiguration config) -> Platform.runLater(() -> this.onConfigurationSuccess(config)),
        error -> Platform.runLater(() -> this.onConnectionFailure(error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  @Override
  public void onConfigurationSuccess(HubConfiguration config) {
    this.hubConfig.set(config);
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
      .uri(baseUrl.getValue() + endpoint)
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
  public ObjectProperty<HubConfiguration> hubConfigProperty() {
    return hubConfig;
  }

  @Override
  public ObjectProperty<LabStatus> statusProperty() {
    return status;
  }

  @Override
  public StringProperty baseUrlProperty() {
    return baseUrl;
  }

  @Override
  public StringProperty accessTokenProperty() {
    return accessToken;
  }

  @Override
  public ObjectProperty<User> authenticatedUserProperty() {
    return authenticatedUser;
  }

  @Override
  public URI computeUri(String path) {
    return URI.create(computeUrl(path));
  }

  @Override
  public String computeUrl(String path) {
    return String.format("%s%s", baseUrl.get(), rgxStripLeadingSlash.matcher(path).replaceAll(""));
  }

  @Override
  public boolean isAuthenticated() {
    return Objects.equals(status.get(), LabStatus.Authenticated);
  }

  @Override
  public void launchPreferencesInBrowser() {
    hostServices.showDocument(baseUrl.get() + "preferences");
  }

  @Override
  public void launchInBrowser() {
    hostServices.showDocument(baseUrl.get());
  }
}
