// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class SegmentMessage   {

  @Valid UUID id;
  @Valid UUID segmentId;
  @Valid SegmentMessageType type;
  @Valid String body;

  /**
   **/
  public SegmentMessage id(UUID id) {
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
  public SegmentMessage segmentId(UUID segmentId) {
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
   **/
  public SegmentMessage type(SegmentMessageType type) {
    this.type = type;
    return this;
  }


  @JsonProperty("type")
  public SegmentMessageType getType() {
    return type;
  }
  public void setType(SegmentMessageType type) {
    this.type = type;
  }

  /**
   **/
  public SegmentMessage body(String body) {
    this.body = body;
    return this;
  }


  @JsonProperty("body")
  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SegmentMessage segmentMessage = (SegmentMessage) o;
    return Objects.equals(this.id, segmentMessage.id) &&
        Objects.equals(this.segmentId, segmentMessage.segmentId) &&
        Objects.equals(this.type, segmentMessage.type) &&
        Objects.equals(this.body, segmentMessage.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, type, body);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SegmentMessage {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    segmentId: ").append(toIndentedString(segmentId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    body: ").append(toIndentedString(body)).append("\n");
    sb.append("}");
    return sb.toString();
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

