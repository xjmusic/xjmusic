package io.xj.gui.services;

import io.xj.hub.HubConfiguration;
import io.xj.hub.tables.pojos.User;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;

public interface LabService {
  void connect();

  void onConnectionChanged();

  void onConnectionSuccess(User user);

  void onConfigurationSuccess(HubConfiguration config);

  void onConnectionFailure(WebClientResponseException error);

  <T> Mono<T> makeAuthenticatedRequest(String endpoint, HttpMethod method, Class<T> responseType);

  void disconnect();

    ObjectProperty<HubConfiguration> hubConfigProperty();

    ObjectProperty<LabStatus> statusProperty();

  StringProperty baseUrlProperty();

  StringProperty accessTokenProperty();

  ObjectProperty<User> authenticatedUserProperty();

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  URI computeUri(String path);

  /**
   Get the URL for a path in the app

   @param path to get URL for
   @return RUL for given path
   */
  String computeUrl(String path);

  /**
   Get the URL for a path in the app

   @return UUID for the lab
   */
  boolean isAuthenticated();

  /**
   Launch the lab preferences window in a browser
   */
  void launchPreferencesInBrowser();

  /**
    Launch the lab in a browser
   */
  void launchInBrowser();
}
