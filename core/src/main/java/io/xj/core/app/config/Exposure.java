// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.config;

import io.xj.core.app.exception.ConfigException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.idea.Idea;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.link.Link;
import io.xj.core.model.voice.Voice;

import com.google.common.collect.ImmutableList;

import org.json.JSONObject;

import java.net.URI;

/**
 ALL APPLICATION CONSTANTS MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public enum Exposure {
  ;

  // wrapError message as JSON output payload.
  public static final String KEY_ERROR_DETAIL = "detail";
  public static final String KEY_ERRORS = "errors";

  // external file references (e.g. audio files)
  public static final String FILE_AUDIO = "audio";
  public static final String FILE_LINK = "link";
  public static final String FILE_CHAIN = "chain";
  public static final String FILE_DOT = ".";
  public static final String FILE_INSTRUMENT = "instrument";
  public static final String FILE_SEPARATOR = "-";
  public static final String FOLDER_SEPARATOR = "/";

  // key special resources (e.g. upload policy)
  public static final String KEY_UPLOAD_ACCESS_KEY = "awsAccessKeyId";
  public static final String KEY_UPLOAD_POLICY = "uploadPolicy";
  public static final String KEY_UPLOAD_URL = "uploadUrl";
  public static final String KEY_WAVEFORM_KEY = "waveformKey";
  public static final String KEY_UPLOAD_POLICY_SIGNATURE = "uploadPolicySignature";
  public static final String KEY_UPLOAD_BUCKET_NAME = "bucketName";
  public static final String KEY_UPLOAD_ACL = "acl";

  // configuration endpoint
  public static final String KEY_CONFIG = "config";
  private static final String KEY_API_BASE_URL = "apiBaseUrl";
  private static final String KEY_AUDIO_BASE_URL = "audioBaseUrl";
  private static final String KEY_BASE_URL = "baseUrl";
  private static final String KEY_CHAIN_CONFIG_TYPES = "chainConfigTypes";
  private static final String KEY_CHAIN_STATES = "chainStates";
  private static final String KEY_CHAIN_TYPES = "chainTypes";
  private static final String KEY_CHOICE_TYPES = "choiceTypes";
  private static final String KEY_IDEA_TYPES = "ideaTypes";
  private static final String KEY_INSTRUMENT_TYPES = "instrumentTypes";
  private static final String KEY_LINK_STATES = "linkStates";
  private static final String KEY_LINK_BASE_URL = "linkBaseUrl";
  private static final String KEY_VOICE_TYPES = "voiceTypes";

  /**
   Get URL String for an audio file, by key

   @param key of audio to retrieve a URL for (empty for base URL)
   @return String
   */
  public static String audioUrl(String key) throws ConfigException {
    return Config.audioBaseUrl() + key;
  }

  /**
   Get URL String for a path within the API

   @param path within API
   @return String
   */
  public static String apiUrlString(String path) {
    return Config.appBaseUrl() + Config.apiPath() + path;
  }

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  public static URI apiURI(String path) {
    return URI.create(apiUrlString(path));
  }

  /**
   Get platform configuration

   @return JSON object
   */
  public static JSONObject configJSON() throws ConfigException {
    JSONObject config = new JSONObject();
    config.put(KEY_API_BASE_URL, Config.appBaseUrl() + Config.apiPath());
    config.put(KEY_AUDIO_BASE_URL, Config.audioBaseUrl());
    config.put(KEY_BASE_URL, Config.appBaseUrl());
    config.put(KEY_CHAIN_CONFIG_TYPES, ImmutableList.copyOf(ChainConfigType.values()));
    config.put(KEY_CHAIN_STATES, Chain.STATES);
    config.put(KEY_CHAIN_TYPES, Chain.TYPES);
    config.put(KEY_CHOICE_TYPES, Choice.TYPES);
    config.put(KEY_IDEA_TYPES, Idea.TYPES);
    config.put(KEY_INSTRUMENT_TYPES, Instrument.TYPES);
    config.put(KEY_LINK_STATES, Link.STATES);
    config.put(KEY_LINK_BASE_URL, Config.linkBaseUrl());
    config.put(KEY_VOICE_TYPES, Voice.TYPES);
    return config;
  }
}
