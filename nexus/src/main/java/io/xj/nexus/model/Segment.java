// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class Segment   {

  private @Valid UUID id;
  private @Valid UUID chainId;
  private @Valid SegmentType type;
  private @Valid SegmentState state;
  private @Valid String beginAt;
  private @Valid String endAt;
  private @Valid String key;
  private @Valid Integer total;
  private @Valid Long offset;
  private @Valid Double density;
  private @Valid Double tempo;
  private @Valid String storageKey;
  private @Valid Double waveformPreroll;
  private @Valid Double waveformPostroll;
  private @Valid String outputEncoder;
  private @Valid Integer delta;
  private @Valid String createdAt;
  private @Valid String updatedAt;

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
   **/
  public Segment beginAt(String beginAt) {
    this.beginAt = beginAt;
    return this;
  }


  @JsonProperty("beginAt")
  public String getBeginAt() {
    return beginAt;
  }
  public void setBeginAt(String beginAt) {
    this.beginAt = beginAt;
  }

  /**
   **/
  public Segment endAt(String endAt) {
    this.endAt = endAt;
    return this;
  }


  @JsonProperty("endAt")
  public String getEndAt() {
    return endAt;
  }
  public void setEndAt(String endAt) {
    this.endAt = endAt;
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
  public Segment outputEncoder(String outputEncoder) {
    this.outputEncoder = outputEncoder;
    return this;
  }


  @JsonProperty("outputEncoder")
  public String getOutputEncoder() {
    return outputEncoder;
  }
  public void setOutputEncoder(String outputEncoder) {
    this.outputEncoder = outputEncoder;
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
        Objects.equals(this.beginAt, segment.beginAt) &&
        Objects.equals(this.endAt, segment.endAt) &&
        Objects.equals(this.key, segment.key) &&
        Objects.equals(this.total, segment.total) &&
        Objects.equals(this.offset, segment.offset) &&
        Objects.equals(this.density, segment.density) &&
        Objects.equals(this.tempo, segment.tempo) &&
        Objects.equals(this.storageKey, segment.storageKey) &&
        Objects.equals(this.waveformPreroll, segment.waveformPreroll) &&
        Objects.equals(this.waveformPostroll, segment.waveformPostroll) &&
        Objects.equals(this.outputEncoder, segment.outputEncoder) &&
        Objects.equals(this.delta, segment.delta) &&
        Objects.equals(this.createdAt, segment.createdAt) &&
        Objects.equals(this.updatedAt, segment.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, chainId, type, state, beginAt, endAt, key, total, offset, density, tempo, storageKey, waveformPreroll, waveformPostroll, outputEncoder, delta, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Segment {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    chainId: ").append(toIndentedString(chainId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    beginAt: ").append(toIndentedString(beginAt)).append("\n");
    sb.append("    endAt: ").append(toIndentedString(endAt)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    density: ").append(toIndentedString(density)).append("\n");
    sb.append("    tempo: ").append(toIndentedString(tempo)).append("\n");
    sb.append("    storageKey: ").append(toIndentedString(storageKey)).append("\n");
    sb.append("    waveformPreroll: ").append(toIndentedString(waveformPreroll)).append("\n");
    sb.append("    waveformPostroll: ").append(toIndentedString(waveformPostroll)).append("\n");
    sb.append("    outputEncoder: ").append(toIndentedString(outputEncoder)).append("\n");
    sb.append("    delta: ").append(toIndentedString(delta)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

