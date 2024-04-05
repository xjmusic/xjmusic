package io.xj.hub;

public class HubConfiguration {
  private String apiBaseUrl;
  private String audioBaseUrl;
  private String streamBaseUrl;
  private String baseUrl;
  private String playerBaseUrl;
  private String shipBaseUrl;

  public String getApiBaseUrl() {
    return apiBaseUrl;
  }

  public HubConfiguration setApiBaseUrl(String apiBaseUrl) {
    this.apiBaseUrl = apiBaseUrl;
    return this;
  }

  public String getAudioBaseUrl() {
    return audioBaseUrl;
  }

  public HubConfiguration setAudioBaseUrl(String audioBaseUrl) {
    this.audioBaseUrl = audioBaseUrl;
    return this;
  }

  public String getStreamBaseUrl() {
    return streamBaseUrl;
  }

  public HubConfiguration setStreamBaseUrl(String streamBaseUrl) {
    this.streamBaseUrl = streamBaseUrl;
    return this;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public HubConfiguration setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public String getPlayerBaseUrl() {
    return playerBaseUrl;
  }

  public HubConfiguration setPlayerBaseUrl(String playerBaseUrl) {
    this.playerBaseUrl = playerBaseUrl;
    return this;
  }

  public String getShipBaseUrl() {
    return shipBaseUrl;
  }

  public HubConfiguration setShipBaseUrl(String shipBaseUrl) {
    this.shipBaseUrl = shipBaseUrl;
    return this;
  }
}
