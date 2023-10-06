// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import io.netty.resolver.DefaultAddressResolverGroup;
import io.xj.hub.HubConfiguration;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.util.StringUtils;
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
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class LabServiceImpl implements LabService {
  Logger LOG = LoggerFactory.getLogger(LabServiceImpl.class);
  private final Preferences prefs = Preferences.userNodeForPackage(LabServiceImpl.class);
  private final HostServices hostServices;
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

    baseUrl.set(prefs.get("baseUrl", defaultLabBaseUrl));
    baseUrl.addListener((observable, prior, value) -> {
      prefs.put("baseUrl", value);
      if (Objects.isNull(value)) {
        return;
      }
      if (!value.endsWith("/")) {
        this.baseUrl.set(this.baseUrl.getValue() + '/');
      }
      LOG.info("Lab URL changed to: " + this.baseUrl.getValue());
    });

    HttpClient httpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);
    webClient = WebClient.builder()
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .build();

    hubConfig.set(new HubConfiguration()
      .setApiBaseUrl(defaultLabBaseUrl)
      .setAudioBaseUrl(audioBaseUrl)
      .setBaseUrl(defaultLabBaseUrl)
      .setShipBaseUrl(shipBaseUrl)
      .setStreamBaseUrl(streamBaseUrl));
    hubConfig.addListener((observable, prior, value) ->
      LOG.info("Lab configured: " +
        "Base: " + value.getBaseUrl() + ", " +
        "API: " + value.getApiBaseUrl() + ", " +
        "Audio: " + value.getAudioBaseUrl() + ", " +
        "Ship: " + value.getShipBaseUrl() + ", " +
        "Stream: " + value.getStreamBaseUrl() + ", "));

    accessToken.addListener((observable, prior, value) -> prefs.put("accessToken", value));
    var savedAccessToken = prefs.get("accessToken", null);
    if (!StringUtils.isNullOrEmpty(savedAccessToken)) {
      LOG.info("Found saved access token, connecting to lab...");
      accessToken.set(savedAccessToken);
      this.connect();
    }
  }

  @Override
  public void connect() {
    this.status.set(LabStatus.Connecting);
    makeAuthenticatedRequest("api/2/users/me", HttpMethod.GET, User.class)
      .subscribe(
        (User user) -> Platform.runLater(() -> this.onConnectionSuccess(user)),
        error -> Platform.runLater(() -> this.onConnectionFailure((WebClientResponseException) error)),
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
        error -> Platform.runLater(() -> this.onConnectionFailure((WebClientResponseException) error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  @Override
  public void onConfigurationSuccess(HubConfiguration config) {
    this.hubConfig.set(config);
    this.status.set(LabStatus.Authenticated);
  }

  @Override
  public void onConnectionFailure(WebClientResponseException error) {
    if (Objects.equals(error.getStatusCode(), UNAUTHORIZED)) {
      LOG.warn("Unauthorized for connection to lab!", error);
      this.authenticatedUser.set(null);
      this.status.set(LabStatus.Unauthorized);
    } else {
      LOG.error("Failed to connect to lab!", error);
      this.status.set(LabStatus.Failed);
    }
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
