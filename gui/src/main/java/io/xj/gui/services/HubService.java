package io.xj.gui.services;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
public class HubService {
  private final WebClient webClient;
  private final ObjectProperty<HubStatus> status = new SimpleObjectProperty<>(HubStatus.Initializing);
  private final StringProperty hubUrl = new SimpleStringProperty();
  private final StringProperty hubAccessToken = new SimpleStringProperty();

  @FXML
  private TextField fieldHubUrl;

  @FXML
  private TextField fieldHubAccessToken;

  public HubService() {
    this.webClient = WebClient.builder().build();
    this.status.set(HubStatus.Initializing);
  }

  public void connect(String url, String accessToken) {
    this.hubUrl.set(url);
    this.hubAccessToken.set(accessToken);
    this.status.set(HubStatus.Connecting);

    makeAuthenticatedRequest("/api/2/users/me", HttpMethod.GET, Object.class)
      .subscribe(
        response -> this.status.set(HubStatus.Authenticated),
        error -> {
          if (Objects.nonNull(error)) {
            this.status.set(HubStatus.Unauthorized);
          } else {
            this.status.set(HubStatus.Failed);
          }
        },
        () -> {
          if (this.status.getValue() == HubStatus.Authenticated) {
            this.status.set(HubStatus.Failed);
          }
        });
  }

  public <T> Mono<T> makeAuthenticatedRequest(String endpoint, HttpMethod method, Class<T> responseType) {
    return webClient.method(method)
      .uri(hubUrl.getValue() + endpoint)
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + hubAccessToken.getValue())
      .retrieve()
      .bodyToMono(responseType);
  }

  public void disconnect() {
    this.status.set(HubStatus.Disconnected);
    this.hubUrl.set(null);
    this.hubAccessToken.set(null);
  }

  public HubStatus getStatus() {
    return status.get();
  }

  public ObjectProperty<HubStatus> statusProperty() {
    return status;
  }

  public StringProperty hubUrlProperty() {
    return hubUrl;
  }

  public StringProperty hubAccessTokenProperty() {
    return hubAccessToken;
  }
}
