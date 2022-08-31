// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class SegmentChoice   {

  private @Valid UUID id;
  private @Valid UUID segmentId;
  private @Valid UUID programId;
  private @Valid UUID programSequenceId;
  private @Valid UUID programSequenceBindingId;
  private @Valid UUID programVoiceId;
  private @Valid UUID instrumentId;
  private @Valid Integer deltaIn;
  private @Valid Integer deltaOut;
  private @Valid Boolean mute;
  private @Valid String instrumentType;
  private @Valid String instrumentMode;
  private @Valid String programType;

  /**
   **/
  public SegmentChoice id(UUID id) {
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
  public SegmentChoice segmentId(UUID segmentId) {
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
  public SegmentChoice programId(UUID programId) {
    this.programId = programId;
    return this;
  }


  @JsonProperty("programId")
  public UUID getProgramId() {
    return programId;
  }
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   **/
  public SegmentChoice programSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
    return this;
  }


  @JsonProperty("programSequenceId")
  public UUID getProgramSequenceId() {
    return programSequenceId;
  }
  public void setProgramSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
  }

  /**
   **/
  public SegmentChoice programSequenceBindingId(UUID programSequenceBindingId) {
    this.programSequenceBindingId = programSequenceBindingId;
    return this;
  }


  @JsonProperty("programSequenceBindingId")
  public UUID getProgramSequenceBindingId() {
    return programSequenceBindingId;
  }
  public void setProgramSequenceBindingId(UUID programSequenceBindingId) {
    this.programSequenceBindingId = programSequenceBindingId;
  }

  /**
   **/
  public SegmentChoice programVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
    return this;
  }


  @JsonProperty("programVoiceId")
  public UUID getProgramVoiceId() {
    return programVoiceId;
  }
  public void setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
  }

  /**
   **/
  public SegmentChoice instrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }


  @JsonProperty("instrumentId")
  public UUID getInstrumentId() {
    return instrumentId;
  }
  public void setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
  }

  /**
   **/
  public SegmentChoice deltaIn(Integer deltaIn) {
    this.deltaIn = deltaIn;
    return this;
  }


  @JsonProperty("deltaIn")
  public Integer getDeltaIn() {
    return deltaIn;
  }
  public void setDeltaIn(Integer deltaIn) {
    this.deltaIn = deltaIn;
  }

  /**
   **/
  public SegmentChoice deltaOut(Integer deltaOut) {
    this.deltaOut = deltaOut;
    return this;
  }


  @JsonProperty("deltaOut")
  public Integer getDeltaOut() {
    return deltaOut;
  }
  public void setDeltaOut(Integer deltaOut) {
    this.deltaOut = deltaOut;
  }

  /**
   **/
  public SegmentChoice mute(Boolean mute) {
    this.mute = mute;
    return this;
  }


  @JsonProperty("mute")
  public Boolean getMute() {
    return mute;
  }
  public void setMute(Boolean mute) {
    this.mute = mute;
  }

  /**
   **/
  public SegmentChoice instrumentType(String instrumentType) {
    this.instrumentType = instrumentType;
    return this;
  }


  @JsonProperty("instrumentType")
  public String getInstrumentType() {
    return instrumentType;
  }
  public void setInstrumentType(String instrumentType) {
    this.instrumentType = instrumentType;
  }

  /**
   **/
  public SegmentChoice instrumentMode(String instrumentMode) {
    this.instrumentMode = instrumentMode;
    return this;
  }


  @JsonProperty("instrumentMode")
  public String getInstrumentMode() {
    return instrumentMode;
  }
  public void setInstrumentMode(String instrumentMode) {
    this.instrumentMode = instrumentMode;
  }

  /**
   **/
  public SegmentChoice programType(String programType) {
    this.programType = programType;
    return this;
  }


  @JsonProperty("programType")
  public String getProgramType() {
    return programType;
  }
  public void setProgramType(String programType) {
    this.programType = programType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SegmentChoice segmentChoice = (SegmentChoice) o;
    return Objects.equals(this.id, segmentChoice.id) &&
        Objects.equals(this.segmentId, segmentChoice.segmentId) &&
        Objects.equals(this.programId, segmentChoice.programId) &&
        Objects.equals(this.programSequenceId, segmentChoice.programSequenceId) &&
        Objects.equals(this.programSequenceBindingId, segmentChoice.programSequenceBindingId) &&
        Objects.equals(this.programVoiceId, segmentChoice.programVoiceId) &&
        Objects.equals(this.instrumentId, segmentChoice.instrumentId) &&
        Objects.equals(this.deltaIn, segmentChoice.deltaIn) &&
        Objects.equals(this.deltaOut, segmentChoice.deltaOut) &&
        Objects.equals(this.mute, segmentChoice.mute) &&
        Objects.equals(this.instrumentType, segmentChoice.instrumentType) &&
        Objects.equals(this.instrumentMode, segmentChoice.instrumentMode) &&
        Objects.equals(this.programType, segmentChoice.programType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, programId, programSequenceId, programSequenceBindingId, programVoiceId, instrumentId, deltaIn, deltaOut, mute, instrumentType, instrumentMode, programType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SegmentChoice {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    segmentId: ").append(toIndentedString(segmentId)).append("\n");
    sb.append("    programId: ").append(toIndentedString(programId)).append("\n");
    sb.append("    programSequenceId: ").append(toIndentedString(programSequenceId)).append("\n");
    sb.append("    programSequenceBindingId: ").append(toIndentedString(programSequenceBindingId)).append("\n");
    sb.append("    programVoiceId: ").append(toIndentedString(programVoiceId)).append("\n");
    sb.append("    instrumentId: ").append(toIndentedString(instrumentId)).append("\n");
    sb.append("    deltaIn: ").append(toIndentedString(deltaIn)).append("\n");
    sb.append("    deltaOut: ").append(toIndentedString(deltaOut)).append("\n");
    sb.append("    mute: ").append(toIndentedString(mute)).append("\n");
    sb.append("    instrumentType: ").append(toIndentedString(instrumentType)).append("\n");
    sb.append("    instrumentMode: ").append(toIndentedString(instrumentMode)).append("\n");
    sb.append("    programType: ").append(toIndentedString(programType)).append("\n");
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

