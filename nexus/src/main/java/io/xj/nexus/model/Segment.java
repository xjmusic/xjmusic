// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.xj.hub.util.ValueUtils;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


public class Segment {
  @JsonIgnore
  public static final int DELTA_UNLIMITED = -1;
  @JsonIgnore
  public static final String EXTENSION_SEPARATOR = ".";
  @JsonIgnore
  public static final String WAV_EXTENSION = "wav";
  UUID id;
  UUID chainId;
  SegmentType type;
  SegmentState state;
  Long beginAtChainMicros; // Segment begin-at time in microseconds since beginning of chain
  @Nullable
  Long durationMicros;
  String key;
  Integer total;
  Long offset;
  Double density;
  Double tempo;
  String storageKey;
  Double waveformPreroll;
  Double waveformPostroll;
  Integer delta;
  String createdAt;
  String updatedAt;

  public Segment() {
    createdAt = computeAtNow();
    updatedAt = computeAtNow();
  }

  /**
   **/
  public Segment id(UUID id) {
    this.id = id;
    return this;
  }


  @JsonProperty("id")
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  /**
   **/
  public Segment chainId(UUID chainId) {
    this.chainId = chainId;
    return this;
  }


  @JsonProperty("chainId")
  public UUID getChainId() {
    return chainId;
  }

  public void setChainId(UUID chainId) {
    this.chainId = chainId;
  }

  /**
   **/
  public Segment type(SegmentType type) {
    this.type = type;
    return this;
  }


  @JsonProperty("type")
  public SegmentType getType() {
    return type;
  }

  public void setType(SegmentType type) {
    this.type = type;
  }

  /**
   **/
  public Segment state(SegmentState state) {
    this.state = state;
    return this;
  }


  @JsonProperty("state")
  public SegmentState getState() {
    return state;
  }

  public void setState(SegmentState state) {
    this.state = state;
  }

  /**
   Segment begin-at time in microseconds since beginning of chain
   **/
  public Segment beginAtChainMicros(long chainMicros) {
    this.beginAtChainMicros = chainMicros;
    return this;
  }


  @JsonProperty("beginAtChainMicros")
  public Long getBeginAtChainMicros() {
    return beginAtChainMicros;
  }

  public void setBeginAtChainMicros(long micros) {
    this.beginAtChainMicros = micros;
  }

  /**
   **/
  public Segment durationMicros(@Nullable Long micros) {
    this.durationMicros = micros;
    return this;
  }


  @JsonProperty("durationMicros")
  @Nullable
  public Long getDurationMicros() {
    return durationMicros;
  }

  public void setDurationMicros(@Nullable Long micros) {
    this.durationMicros = micros;
  }

  /**
   **/
  public Segment key(String key) {
    this.key = key;
    return this;
  }


  @JsonProperty("key")
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  /**
   **/
  public Segment total(Integer total) {
    this.total = total;
    return this;
  }


  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   **/
  public Segment offset(Long offset) {
    this.offset = offset;
    return this;
  }


  @JsonProperty("offset")
  public Long getOffset() {
    return offset;
  }

  public void setOffset(Long offset) {
    this.offset = offset;
  }

  /**
   **/
  public Segment density(Double density) {
    this.density = density;
    return this;
  }


  @JsonProperty("density")
  public Double getDensity() {
    return density;
  }

  public void setDensity(Double density) {
    this.density = density;
  }

  /**
   **/
  public Segment tempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }


  @JsonProperty("tempo")
  public Double getTempo() {
    return tempo;
  }

  public void setTempo(Double tempo) {
    this.tempo = tempo;
  }

  /**
   **/
  public Segment storageKey(String storageKey) {
    this.storageKey = storageKey;
    return this;
  }


  @JsonProperty("storageKey")
  public String getStorageKey() {
    return storageKey;
  }

  public void setStorageKey(String storageKey) {
    this.storageKey = storageKey;
  }

  /**
   **/
  public Segment waveformPreroll(Double waveformPreroll) {
    this.waveformPreroll = waveformPreroll;
    return this;
  }


  @JsonProperty("waveformPreroll")
  public Double getWaveformPreroll() {
    return waveformPreroll;
  }

  public void setWaveformPreroll(Double waveformPreroll) {
    this.waveformPreroll = waveformPreroll;
  }

  @JsonProperty("waveformPostroll")
  public Double getWaveformPostroll() {
    return waveformPostroll;
  }

  public void setWaveformPostroll(Double waveformPostroll) {
    this.waveformPostroll = waveformPostroll;
  }

  /**
   **/
  public Segment delta(Integer delta) {
    this.delta = delta;
    return this;
  }


  @JsonProperty("delta")
  public Integer getDelta() {
    return delta;
  }

  public void setDelta(Integer delta) {
    this.delta = delta;
  }

  @JsonProperty("createdAt")
  public String getCreatedAt() {
    return createdAt;
  }

  public Segment setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  @JsonProperty("updatedAt")
  public String getUpdatedAt() {
    return updatedAt;
  }

  public Segment setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public void setUpdatedNow() {
    updatedAt = computeAtNow();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Segment segment = (Segment) o;
    return Objects.equals(this.id, segment.id) &&
      Objects.equals(this.chainId, segment.chainId) &&
      Objects.equals(this.type, segment.type) &&
      Objects.equals(this.state, segment.state) &&
      Objects.equals(this.beginAtChainMicros, segment.beginAtChainMicros) &&
      Objects.equals(this.durationMicros, segment.durationMicros) &&
      Objects.equals(this.key, segment.key) &&
      Objects.equals(this.total, segment.total) &&
      Objects.equals(this.offset, segment.offset) &&
      Objects.equals(this.density, segment.density) &&
      Objects.equals(this.tempo, segment.tempo) &&
      Objects.equals(this.storageKey, segment.storageKey) &&
      Objects.equals(this.waveformPreroll, segment.waveformPreroll) &&
      Objects.equals(this.waveformPostroll, segment.waveformPostroll) &&
      Objects.equals(this.delta, segment.delta) &&
      Objects.equals(this.createdAt, segment.createdAt) &&
      Objects.equals(this.updatedAt, segment.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, chainId, type, state, beginAtChainMicros, durationMicros, key, total, offset, density, tempo, storageKey, waveformPreroll, waveformPostroll, delta, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    return "class Segment {\n" +
      "    id: " + toIndentedString(id) + "\n" +
      "    chainId: " + toIndentedString(chainId) + "\n" +
      "    type: " + toIndentedString(type) + "\n" +
      "    state: " + toIndentedString(state) + "\n" +
      "    beginAtChainMicros: " + toIndentedString(beginAtChainMicros) + "\n" +
      "    durationMicros: " + toIndentedString(durationMicros) + "\n" +
      "    key: " + toIndentedString(key) + "\n" +
      "    total: " + toIndentedString(total) + "\n" +
      "    offset: " + toIndentedString(offset) + "\n" +
      "    density: " + toIndentedString(density) + "\n" +
      "    tempo: " + toIndentedString(tempo) + "\n" +
      "    storageKey: " + toIndentedString(storageKey) + "\n" +
      "    waveformPreroll: " + toIndentedString(waveformPreroll) + "\n" +
      "    waveformPostroll: " + toIndentedString(waveformPostroll) + "\n" +
      "    delta: " + toIndentedString(delta) + "\n" +
      "    createdAt: " + toIndentedString(createdAt) + "\n" +
      "    updatedAt: " + toIndentedString(updatedAt) + "\n" +
      "}";
  }

  /**
   Convert the given object to string with each line indented by 4 spaces
   (except the first line).
   */
  String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  String computeAtNow() {
    return ValueUtils.formatIso8601UTC(Instant.now());
  }
}

