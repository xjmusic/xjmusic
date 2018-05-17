// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pick;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

import javax.annotation.Nullable;
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
  private BigInteger arrangementId;
  private BigInteger morphId;
  private BigInteger audioId;
  private Double start;
  private Double length;
  private Double amplitude;
  private Double pitch;

  public BigInteger getArrangementId() {
    return arrangementId;
  }

  public Pick setArrangementId(BigInteger arrangementId) {
    this.arrangementId = arrangementId;
    return this;
  }

  @Nullable
  public BigInteger getMorphId() {
    return morphId;
  }

  public Pick setMorphId(BigInteger morphId) {
    this.morphId = morphId;
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
    if (this.arrangementId == null) {
      throw new BusinessException("Arrangement ID is required.");
    }
    if (this.audioId == null) {
      throw new BusinessException("Audio ID is required.");
    }
    if (this.start == null) {
      throw new BusinessException("Start is required.");
    }
    if (this.length == null || this.length == (double) 0) {
      throw new BusinessException("Length is required.");
    }
    if (this.amplitude == null || this.amplitude == (double) 0) {
      throw new BusinessException("Amplitude is required.");
    }
    if (this.pitch == null || this.pitch == (double) 0) {
      throw new BusinessException("Pitch is required.");
    }
  }

}
