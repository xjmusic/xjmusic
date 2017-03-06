// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.config;

import org.json.JSONObject;

import java.net.URI;

/**
 * ALL APPLICATION CONSTANTS MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public abstract class Exposure {

  // wrapError message as JSON output payload.
  public static final String KEY_ERRORS = "errors";
  public static final String KEY_ERROR_DETAIL = "detail";

  // external file references (e.g. audio files)
  public static final String FILE_EXTENSION = "wav";
  public static final String FILE_AUDIO = "audio";
  public static final String FILE_INSTRUMENT = "instrument";
  public static final String FILE_SEPARATOR = "-";
  public static final String FILE_DOT = ".";

  // key special resources (e.g. upload policy)
  public static final String KEY_UPLOAD_URL = "uploadUrl";
  public static final String KEY_UPLOAD_POLICY = "uploadPolicy";
  public static final String KEY_WAVEFORM_KEY = "waveformKey";

  // configuration endpoint
  public static final String KEY_CONFIG_AUDIO_BASE_URL = "audioBaseUrl";
  public static final String KEY_CONFIG_BASE_URL = "baseUrl";
  public static final String KEY_CONFIG_API_BASE_URL = "apiBaseUrl";
  public static final String KEY_CONFIG_CHAIN_STATES = "chainStates";
  public static final String KEY_CONFIG = "config";
  public static final String KEY_UPLOAD_ACCESS_KEY = "uploadAccessKey";

  /**
   * Get URL String for an audio file, by key
   * @param key of audio to retrieve a URL for (empty for base URL)
   * @return String
   */
  public static String audioUrl(String key) {
    return Config.audioBaseUrl() + key;
  }

  /**
   * Get URL String for a path within the API
   * @param path within API
   * @return String
   */
  public static String apiUrlString(String path) {
    return Config.appBaseUrl() + Config.apiPath() + path;
  }

  /**
   * Get URI object for a path within the API
   * @param path within API
   * @return String
   */
  public static URI apiURI(String path) {
    return URI.create(apiUrlString(path));
  }

  /**
   * Get platform configuration
   * @return JSON object
   */
  public static JSONObject configJSON() {
    JSONObject config = new JSONObject();
    config.put(KEY_CONFIG_BASE_URL, Config.appBaseUrl());
    config.put(KEY_CONFIG_API_BASE_URL, Config.appBaseUrl() + Config.apiPath());
    config.put(KEY_CONFIG_AUDIO_BASE_URL, Config.audioBaseUrl());
    config.put(KEY_CONFIG_CHAIN_STATES, Config.chainStates());
    return config;
  }
}
