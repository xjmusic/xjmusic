// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.Objects;
import java.util.UUID;


public class Segment {

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

  /**
   **/
  public Segment waveformPostroll(Double waveformPostroll) {
    this.waveformPostroll = waveformPostroll;
    return this;
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

  /**
   **/
  public Segment createdAt(String createdAt) {
    this.createdAt = createdAt;
    return this;
  }


  @JsonProperty("createdAt")
  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public Segment updatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }


  @JsonProperty("updatedAt")
  public String getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
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
    StringBuilder sb = new StringBuilder();
    sb.append("class Segment {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    chainId: ").append(toIndentedString(chainId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    beginAtChainMicros: ").append(toIndentedString(beginAtChainMicros)).append("\n");
    sb.append("    durationMicros: ").append(toIndentedString(durationMicros)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    density: ").append(toIndentedString(density)).append("\n");
    sb.append("    tempo: ").append(toIndentedString(tempo)).append("\n");
    sb.append("    storageKey: ").append(toIndentedString(storageKey)).append("\n");
    sb.append("    waveformPreroll: ").append(toIndentedString(waveformPreroll)).append("\n");
    sb.append("    waveformPostroll: ").append(toIndentedString(waveformPostroll)).append("\n");
    sb.append("    delta: ").append(toIndentedString(delta)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
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
}

