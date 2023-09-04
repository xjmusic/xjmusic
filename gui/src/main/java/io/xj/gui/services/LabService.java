package io.xj.gui.services;

import io.xj.hub.tables.pojos.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LabService {
  void connect();

  void onConnectionChanged();

  void onConnectionSuccess(User user);

  void onConnectionFailure(Throwable error);

  <T> Mono<T> makeAuthenticatedRequest(String endpoint, HttpMethod method, Class<T> responseType);

  void disconnect();

  ObjectProperty<LabStatus> statusProperty();

  StringProperty urlProperty();

  StringProperty accessTokenProperty();

  ObjectProperty<User> authenticatedUserProperty();
}
