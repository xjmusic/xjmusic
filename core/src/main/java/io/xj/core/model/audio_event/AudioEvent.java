// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio_event;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.event.Event;
import io.xj.core.util.Text;

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
public class AudioEvent extends Event {
  public static final String KEY_ONE = "audioEvent";
  public static final String KEY_MANY = "audioEvents";
  private BigInteger audioId;

  @Override
  public AudioEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public AudioEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public AudioEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  @Override
  public AudioEvent setPosition(Double position) {
    this.position = position;
    return this;
  }

  @Override
  public AudioEvent setTonality(Double tonality) {
    this.tonality = tonality;
    return this;
  }

  @Override
  public AudioEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  public BigInteger getAudioId() {
    return audioId;
  }

  public AudioEvent setAudioId(BigInteger audioId) {
    this.audioId = audioId;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    super.validate();
    if (this.audioId == null) {
      throw new BusinessException("Audio ID is required.");
    }
  }

}
