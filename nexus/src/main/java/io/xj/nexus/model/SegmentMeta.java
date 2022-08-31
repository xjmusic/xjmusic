// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;

/**
 Segment has metadata for XJ to persist "notes in the margin" of the composition for itself to read https://www.pivotaltracker.com/story/show/183135787
 */
public class SegmentMeta {

  private @Valid UUID id;
  private @Valid UUID segmentId;
  private @Valid String key;
  private @Valid String value;

  /**
   *
   **/
  public SegmentMeta id(UUID id) {
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
   *
   **/
  public SegmentMeta segmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }


  @JsonProperty("segmentId")
  public UUID getSegmentId() {
    return segmentId;
  }

  public void setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
  }

  /**
   *
   **/
  public SegmentMeta key(String key) {
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
   *
   **/
  public SegmentMeta value(String value) {
    this.value = value;
    return this;
  }


  @JsonProperty("value")
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SegmentMeta segmentMeta = (SegmentMeta) o;
    return Objects.equals(this.id, segmentMeta.id) &&
      Objects.equals(this.segmentId, segmentMeta.segmentId) &&
      Objects.equals(this.key, segmentMeta.key) &&
      Objects.equals(this.value, segmentMeta.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, key, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SegmentMeta {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    segmentId: ").append(toIndentedString(segmentId)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   Convert the given object to string with each line indented by 4 spaces
   (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

