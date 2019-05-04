//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.config;

import com.google.common.collect.Maps;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceType;

import java.util.Map;

public enum API {
  ;
  static final String API_BASE_URL = "apiBaseUrl";
  static final String AUDIO_BASE_URL = "audioBaseUrl";
  static final String BASE_URL = "baseUrl";
  static final String CHAIN_CONFIG_TYPES = "chainConfigTypes";
  static final String CHAIN_STATES = "chainStates";
  static final String CHAIN_TYPES = "chainTypes";
  static final String CHOICE_TYPES = "choiceTypes";
  static final String INSTRUMENT_TYPES = "instrumentTypes";
  static final String PATTERN_DETAIL_TYPES = "patternDetailTypes";
  static final String PATTERN_TYPES = "patternTypes";
  static final String SEGMENT_BASE_URL = "segmentBaseUrl";
  static final String SEGMENT_STATES = "segmentStates";
  static final String SEQUENCE_TYPES = "sequenceTypes";
  static final String VOICE_TYPES = "voiceTypes";

  /**
   Get platform configuration

   @return JSON object
   */
  public static Map<String, Object> getConfig() throws CoreException {
    Map<String, Object> config = Maps.newConcurrentMap();
    config.put(API_BASE_URL, Config.appBaseUrl() + Config.apiPath());
    config.put(AUDIO_BASE_URL, Config.audioBaseUrl());
    config.put(SEGMENT_BASE_URL, Config.segmentBaseUrl());
    config.put(BASE_URL, Config.appBaseUrl());
    config.put(CHAIN_CONFIG_TYPES, ChainConfigType.stringValues());
    config.put(CHAIN_STATES, ChainState.stringValues());
    config.put(CHAIN_TYPES, ChainType.stringValues());
    config.put(SEGMENT_STATES, SegmentState.stringValues());
    config.put(CHOICE_TYPES, SequenceType.stringValues());
    config.put(SEQUENCE_TYPES, SequenceType.stringValues());
    config.put(PATTERN_TYPES, PatternType.stringValues());
    config.put(PATTERN_DETAIL_TYPES, PatternType.stringValuesForDetailSequence());
    config.put(INSTRUMENT_TYPES, InstrumentType.stringValues());
    config.put(VOICE_TYPES, InstrumentType.stringValues());
    return config;
  }

}
