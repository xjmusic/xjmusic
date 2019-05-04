// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.config;

import io.xj.core.exception.CoreException;

import java.net.URI;

/**
 ALL APPLICATION CONSTANTS MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public interface Exposure {
  // external file references (e.g. audio files)
  String FILE_AUDIO = "audio";
  String FILE_SEGMENT = "segment";
  String FILE_CHAIN = "chain";
  String FILE_DOT = ".";
  String FILE_INSTRUMENT = "instrument";
  String FILE_SEPARATOR = "-";

  // key special resources (e.g. upload policy)
  String KEY_UPLOAD_ACCESS_KEY = "awsAccessKeyId";
  String KEY_UPLOAD_POLICY = "uploadPolicy";
  String KEY_UPLOAD_URL = "uploadUrl";
  String KEY_WAVEFORM_KEY = "waveformKey";
  String KEY_UPLOAD_POLICY_SIGNATURE = "uploadPolicySignature";
  String KEY_UPLOAD_BUCKET_NAME = "bucketName";
  String KEY_UPLOAD_ACL = "acl";

  // configuration endpoint
  String CONFIG = "config";
  String PICKS = "picks";
  String STATS = "stats";
  String TYPE = "type";
  String CHOICES = "choices";
  String ARRANGEMENTS = "arrangements";
  String SEGMENT_MEMES = "segmentMemes";
  String SEGMENT_CHORDS = "segmentChords";
  String CHORDS = "chords";
  String SEGMENT_MESSAGES = "segmentMessages";
  String MESSAGES = "messages";
  String MEMES = "memes";

  /**
   Get URL String for an audio file, by key

   @param key of audio to retrieve a URL for (empty for base URL)
   @return String
   */
  static String audioUrl(String key) throws CoreException {
    return Config.audioBaseUrl() + key;
  }

  /**
   Get URL String for a path within the API

   @param path within API
   @return String
   */
  static String apiUrlString(String path) {
    return Config.appBaseUrl() + Config.apiPath() + path;
  }

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  static URI apiURI(String path) {
    return URI.create(apiUrlString(path));
  }

}
