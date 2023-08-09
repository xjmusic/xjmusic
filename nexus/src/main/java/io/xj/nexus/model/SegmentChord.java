// Copyright (c) 1999-2022, XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;


public class SegmentChord {

  UUID id;
  UUID segmentId;
  Double position;
  String name;

  /**
   *
   **/
  public SegmentChord id(UUID id) {
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
  public SegmentChord segmentId(UUID segmentId) {
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
  public SegmentChord position(Double position) {
    this.position = position;
    return this;
  }


  @JsonProperty("position")
  public Double getPosition() {
    return position;
  }

  public void setPosition(Double position) {
    this.position = position;
  }

  /**
   *
   **/
  public SegmentChord name(String name) {
    this.name = name;
    return this;
  }


  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SegmentChord segmentChord = (SegmentChord) o;
    return Objects.equals(this.id, segmentChord.id) &&
      Objects.equals(this.segmentId, segmentChord.segmentId) &&
      Objects.equals(this.position, segmentChord.position) &&
      Objects.equals(this.name, segmentChord.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, position, name);
  }

  @Override
  public String toString() {
    return "class SegmentChord {\n" +
      "    id: " + toIndentedString(id) + "\n" +
      "    segmentId: " + toIndentedString(segmentId) + "\n" +
      "    atSegmentMicros: " + toIndentedString(position) + "\n" +
      "    name: " + toIndentedString(name) + "\n" +
      "}";
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

