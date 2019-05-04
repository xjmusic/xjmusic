// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pick;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;

import java.math.BigInteger;
import java.util.Collection;
import java.util.UUID;

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
public class Pick extends SegmentEntity {
  private static final Double LENGTH_MINIMUM = 0.01;
  private static final Double AMPLITUDE_MINIMUM = 0.01;
  private static final Double PITCH_MINIMUM = 1.0;
  private UUID arrangementUuid;
  private BigInteger audioId;
  private BigInteger patternEventId;
  private Double start;
  private Double length;
  private Double amplitude;
  private Double pitch;
  private String inflection;
  private BigInteger voiceId;

  public static Collection<Pick> aggregate(Collection<Segment> segments) {
    Collection<Pick> aggregate = Lists.newArrayList();
    segments.forEach(segment -> aggregate.addAll(segment.getPicks()));
    return aggregate;
  }

  public UUID getArrangementUuid() {
    return arrangementUuid;
  }

  public Pick setArrangementUuid(UUID arrangementUuid) {
    this.arrangementUuid = arrangementUuid;
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
  public Pick setUuid(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    if (null == arrangementUuid) {
      throw new CoreException("Arrangement ID is required.");
    }
    if (null == patternEventId) {
      throw new CoreException("Pattern Event ID is required.");
    }
    if (null == audioId) {
      throw new CoreException("Audio ID is required.");
    }
    if (null == voiceId) {
      throw new CoreException("Voice ID is required.");
    }
    if (null == start) {
      throw new CoreException("Start is required.");
    }
    if (null == length) {
      throw new CoreException("Length is required.");
    }
    if (0 >= length) {
      length = LENGTH_MINIMUM;
    }
    if (null == amplitude) {
      throw new CoreException("Amplitude is required.");
    }
    if (0 >= amplitude) {
      amplitude = AMPLITUDE_MINIMUM;
    }
    if (null == pitch) {
      throw new CoreException("Pitch is required.");
    }
    if (0 >= pitch) {
      pitch = PITCH_MINIMUM;
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
