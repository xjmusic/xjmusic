// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pick;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;
import org.json.JSONObject;

import java.math.BigInteger;

/**
 POJO for persisting data in memory while performing business logic,
 or decoding messages received by JAX-RS resources.
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Pick extends Entity {
  public static final String KEY_ONE = "pick";
  public static final String KEY_MANY = "picks";
  public static final String KEY_ARRANGEMENT_ID = "arrangementId";
  public static final String KEY_AUDIO_ID = "audioId";
  public static final String KEY_PATTERN_EVENT_ID = "patternEventId";
  public static final String KEY_START = "start";
  public static final String KEY_LENGTH = "length";
  public static final String KEY_AMPLITUDE = "amplitude";
  public static final String KEY_PITCH = "pitch";
  public static final String KEY_INFLECTION = "inflection";
  private BigInteger arrangementId;
  private BigInteger audioId;
  private BigInteger patternEventId;
  private Double start;
  private Double length;
  private Double amplitude;
  private Double pitch;
  private String inflection;
  private BigInteger voiceId;

  public BigInteger getArrangementId() {
    return arrangementId;
  }

  public Pick setArrangementId(BigInteger arrangementId) {
    this.arrangementId = arrangementId;
    return this;
  }

  public BigInteger getAudioId() {
    return audioId;
  }

  public Pick setAudioId(BigInteger audioId) {
    this.audioId = audioId;
    return this;
  }

  /**
   Start position from beginning of segment, in Seconds

   @return seconds
   */
  public Double getStart() {
    return start;
  }


  public Pick setStart(Double start) {
    this.start = start;
    return this;
  }

  /**
   Length from Start position, in Seconds

   @return seconds
   */
  public Double getLength() {
    return length;
  }

  public Pick setLength(Double length) {
    this.length = length;
    return this;
  }

  public Double getAmplitude() {
    return amplitude;
  }

  public Pick setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
    return this;
  }

  public Double getPitch() {
    return pitch;
  }

  public Pick setPitch(Double pitch) {
    this.pitch = pitch;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return arrangementId;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == arrangementId) {
      throw new BusinessException("Arrangement ID is required.");
    }
    if (null == patternEventId) {
      throw new BusinessException("Pattern Event ID is required.");
    }
    if (null == audioId) {
      throw new BusinessException("Audio ID is required.");
    }
    if (null == voiceId) {
      throw new BusinessException("Voice ID is required.");
    }
    if (null == start) {
      throw new BusinessException("Start is required.");
    }
    if (null == length || (double) 0 == length) {
      throw new BusinessException("Length is required.");
    }
    if (null == amplitude || (double) 0 == amplitude) {
      throw new BusinessException("Amplitude is required.");
    }
    if (null == pitch || (double) 0 == pitch) {
      throw new BusinessException("Pitch is required.");
    }
  }

  public String getInflection() {
    return inflection;
  }

  public Pick setInflection(String inflection) {
    this.inflection = inflection;
    return this;
  }

  public BigInteger getPatternEventId() {
    return patternEventId;
  }

  public Pick setPatternEventId(BigInteger patternEventId) {
    this.patternEventId = patternEventId;
    return this;
  }

  public BigInteger getVoiceId() {
    return voiceId;
  }

  public Pick setVoiceId(BigInteger voiceId) {
    this.voiceId = voiceId;
    return this;
  }
}
