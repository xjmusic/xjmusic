// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;


public class SegmentChoiceArrangement {

  UUID id;
  Integer segmentId;
  UUID segmentChoiceId;
  UUID programSequencePatternId;

  /**
   **/
  public SegmentChoiceArrangement id(UUID id) {
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
  public SegmentChoiceArrangement segmentId(Integer segmentId) {
    this.segmentId = segmentId;
    return this;
  }


  @JsonProperty("segmentId")
  public Integer getSegmentId() {
    return segmentId;
  }

  public void setSegmentId(Integer segmentId) {
    this.segmentId = segmentId;
  }

  /**
   **/
  public SegmentChoiceArrangement segmentChoiceId(UUID segmentChoiceId) {
    this.segmentChoiceId = segmentChoiceId;
    return this;
  }


  @JsonProperty("segmentChoiceId")
  public UUID getSegmentChoiceId() {
    return segmentChoiceId;
  }

  public void setSegmentChoiceId(UUID segmentChoiceId) {
    this.segmentChoiceId = segmentChoiceId;
  }

  /**
   **/
  public SegmentChoiceArrangement programSequencePatternId(UUID programSequencePatternId) {
    this.programSequencePatternId = programSequencePatternId;
    return this;
  }


  @JsonProperty("programSequencePatternId")
  public UUID getProgramSequencePatternId() {
    return programSequencePatternId;
  }

  public void setProgramSequencePatternId(UUID programSequencePatternId) {
    this.programSequencePatternId = programSequencePatternId;
  }


  @Override
  public boolean equals(Object o) {
    if (this==o) {
      return true;
    }
    if (o==null || getClass()!=o.getClass()) {
      return false;
    }
    SegmentChoiceArrangement segmentChoiceArrangement = (SegmentChoiceArrangement) o;
    return Objects.equals(this.id, segmentChoiceArrangement.id) &&
      Objects.equals(this.segmentId, segmentChoiceArrangement.segmentId) &&
      Objects.equals(this.segmentChoiceId, segmentChoiceArrangement.segmentChoiceId) &&
      Objects.equals(this.programSequencePatternId, segmentChoiceArrangement.programSequencePatternId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, segmentChoiceId, programSequencePatternId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SegmentChoiceArrangement {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    segmentId: ").append(toIndentedString(segmentId)).append("\n");
    sb.append("    segmentChoiceId: ").append(toIndentedString(segmentChoiceId)).append("\n");
    sb.append("    programSequencePatternId: ").append(toIndentedString(programSequencePatternId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   Convert the given object to string with each line indented by 4 spaces
   (except the first line).
   */
  String toIndentedString(Object o) {
    if (o==null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

