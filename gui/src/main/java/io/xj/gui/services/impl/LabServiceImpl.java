// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services.impl;

import io.netty.resolver.DefaultAddressResolverGroup;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabState;
import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.HubProjectList;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.hub_client.HubClientAccess;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
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
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.MOVED_PERMANENTLY;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class LabServiceImpl implements LabService {
  Logger LOG = LoggerFactory.getLogger(LabServiceImpl.class);
  private final Preferences prefs = Preferences.userNodeForPackage(LabServiceImpl.class);
  private final HostServices hostServices;
  final WebClient webClient;
  final ObjectProperty<LabState> state = new SimpleObjectProperty<>(LabState.Offline);
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
    @Value("${stream.base.url}") String streamBaseUrl,
    @Value("${prefs.load}") Boolean usePrefs
  ) {
    this.hostServices = hostServices;

    baseUrl.addListener((o, ov, value) -> {
      if (usePrefs)
        prefs.put("baseUrl", value);
      if (Objects.isNull(value)) {
        return;
      }
      if (!value.endsWith("/")) {
        this.baseUrl.set(this.baseUrl.getValue() + '/');
      }
      LOG.info("Lab URL changed to: " + this.baseUrl.getValue());
    });
    if (usePrefs)
      baseUrl.set(prefs.get("baseUrl", defaultLabBaseUrl));
    else
      baseUrl.set(defaultLabBaseUrl);

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
    hubConfig.addListener((o, ov, value) ->
      LOG.info("Lab configured: " +
        "Base: " + value.getBaseUrl() + ", " +
        "API: " + value.getApiBaseUrl() + ", " +
        "Audio: " + value.getAudioBaseUrl() + ", " +
        "Ship: " + value.getShipBaseUrl() + ", " +
        "Stream: " + value.getStreamBaseUrl() + ", "));

    accessToken.addListener((o, ov, value) -> {
      if (usePrefs)
        prefs.put("accessToken", value);
    });
    var savedAccessToken = usePrefs ? prefs.get("accessToken", null):null;
    if (!StringUtils.isNullOrEmpty(savedAccessToken)) {
      LOG.info("Found saved access token, connecting to lab...");
      accessToken.set(savedAccessToken);
      this.connect();
    }
  }

  @Override
  public void connect() {
    this.state.set(LabState.Connecting);
    makeAuthenticatedRequest("api/2/users/me", HttpMethod.GET, HubContent.class)
      .subscribe(
        (HubContent content) -> Platform.runLater(() -> this.onConnectionSuccess(content.getUsers().stream().findFirst().orElseThrow(() -> new RuntimeException("No user found!")))),
        error -> Platform.runLater(() -> this.onConnectionFailure((Exception) error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  @Override
  public void onConnectionChanged() {
    // no op
  }

  @Override
  public void onConnectionSuccess(User user) {
    this.authenticatedUser.set(user);
    this.state.set(LabState.Configuring);
    makeAuthenticatedRequest("api/2/config", HttpMethod.GET, HubConfiguration.class)
      .subscribe(
        (HubConfiguration config) -> Platform.runLater(() -> this.onConfigurationSuccess(config)),
        error -> Platform.runLater(() -> this.onConnectionFailure((Exception) error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  @Override
  public void onConfigurationSuccess(HubConfiguration config) {
    this.hubConfig.set(config);
    this.state.set(LabState.Authenticated);
  }

  @Override
  public void onConnectionFailure(Exception error) {
    if (error instanceof WebClientResponseException && Objects.equals(((WebClientResponseException) error).getStatusCode(), UNAUTHORIZED)) {
      LOG.warn("Unauthorized for connection to lab!", error);
      this.authenticatedUser.set(null);
      this.state.set(LabState.Unauthorized);
    } else if (error instanceof WebClientResponseException && Objects.equals(((WebClientResponseException) error).getStatusCode(), MOVED_PERMANENTLY)) {
      var location = ((WebClientResponseException) error).getHeaders().getLocation();
      if (Objects.nonNull(location))
        LOG.error("Failed to connect. Lab moved permanently to {}://{}/", location.getScheme(), location.getHost());
      else
        LOG.error("Failed to connect. Lab moved permanently, but no location header found!", error);
      this.state.set(LabState.Failed);
    } else {
      LOG.error("Failed to connect to lab!", error);
      this.state.set(LabState.Failed);
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
    this.state.set(LabState.Offline);
    this.authenticatedUser.set(null);
  }

  @Override
  public ObjectProperty<HubConfiguration> hubConfigProperty() {
    return hubConfig;
  }

  @Override
  public ObjectProperty<LabState> stateProperty() {
    return state;
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
  public BooleanBinding isAuthenticated() {
    return state.isEqualTo(LabState.Authenticated);
  }

  @Override
  public void launchPreferencesInBrowser() {
    hostServices.showDocument(baseUrl.get() + "preferences");
  }

  @Override
  public void launchInBrowser() {
    hostServices.showDocument(baseUrl.get());
  }

  @Override
  public void fetchProjects(Consumer<Collection<Project>> callback) {
    makeAuthenticatedRequest("api/2/projects", HttpMethod.GET, HubProjectList.class)
      .subscribe(
        (HubProjectList content) -> Platform.runLater(() -> callback.accept(content.getProjects())),
        error -> Platform.runLater(() -> this.onConnectionFailure((Exception) error)),
        () -> Platform.runLater(this::onConnectionChanged));
  }

  @Override
  public HubClientAccess getHubClientAccess() {
    return new HubClientAccess()
      .setUserId(authenticatedUser.get().getId())
      .setToken(accessToken.get());
  }
}
