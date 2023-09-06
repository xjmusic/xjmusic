// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;


public class SegmentChordVoicing {

  UUID id;
  UUID segmentId;
  UUID segmentChordId;
  String type;
  String notes;

  /**
   **/
  public SegmentChordVoicing id(UUID id) {
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
  public SegmentChordVoicing segmentId(UUID segmentId) {
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
  public SegmentChordVoicing segmentChordId(UUID segmentChordId) {
    this.segmentChordId = segmentChordId;
    return this;
  }


  @JsonProperty("segmentChordId")
  public UUID getSegmentChordId() {
    return segmentChordId;
  }

  public void setSegmentChordId(UUID segmentChordId) {
    this.segmentChordId = segmentChordId;
  }

  /**
   **/
  public SegmentChordVoicing type(String type) {
    this.type = type;
    return this;
  }


  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public SegmentChordVoicing notes(String notes) {
    this.notes = notes;
    return this;
  }


  @JsonProperty("notes")
  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SegmentChordVoicing segmentChordVoicing = (SegmentChordVoicing) o;
    return Objects.equals(this.id, segmentChordVoicing.id) &&
      Objects.equals(this.segmentId, segmentChordVoicing.segmentId) &&
      Objects.equals(this.segmentChordId, segmentChordVoicing.segmentChordId) &&
      Objects.equals(this.type, segmentChordVoicing.type) &&
      Objects.equals(this.notes, segmentChordVoicing.notes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, segmentChordId, type, notes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SegmentChordVoicing {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    segmentId: ").append(toIndentedString(segmentId)).append("\n");
    sb.append("    segmentChordId: ").append(toIndentedString(segmentChordId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    notes: ").append(toIndentedString(notes)).append("\n");
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

