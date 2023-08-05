// Copyright (c) 1999-2022, XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class SegmentChoice {

  @Valid UUID id;
  @Valid UUID segmentId;
  @Valid UUID programId;
  @Valid UUID programSequenceId;
  @Valid UUID programSequenceBindingId;
  @Valid UUID programVoiceId;
  @Valid UUID instrumentId;
  @Valid Integer deltaIn;
  @Valid Integer deltaOut;
  @Valid Boolean mute;
  @Valid InstrumentType instrumentType;
  @Valid InstrumentMode instrumentMode;
  @Valid ProgramType programType;

  /**
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
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
   *
   **/
  public SegmentChoice instrumentType(InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
    return this;
  }


  @JsonProperty("instrumentType")
  public InstrumentType getInstrumentType() {
    return instrumentType;
  }

  public void setInstrumentType(InstrumentType instrumentType) {
    this.instrumentType = instrumentType;
  }

  /**
   *
   **/
  public SegmentChoice instrumentMode(InstrumentMode instrumentMode) {
    this.instrumentMode = instrumentMode;
    return this;
  }


  @JsonProperty("instrumentMode")
  public InstrumentMode getInstrumentMode() {
    return instrumentMode;
  }

  public void setInstrumentMode(InstrumentMode instrumentMode) {
    this.instrumentMode = instrumentMode;
  }

  /**
   *
   **/
  public SegmentChoice programType(ProgramType programType) {
    this.programType = programType;
    return this;
  }


  @JsonProperty("programType")
  public ProgramType getProgramType() {
    return programType;
  }

  public void setProgramType(ProgramType programType) {
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

    return "class SegmentChoice {\n" +
      "    id: " + toIndentedString(id) + "\n" +
      "    segmentId: " + toIndentedString(segmentId) + "\n" +
      "    programId: " + toIndentedString(programId) + "\n" +
      "    programSequenceId: " + toIndentedString(programSequenceId) + "\n" +
      "    programSequenceBindingId: " + toIndentedString(programSequenceBindingId) + "\n" +
      "    programVoiceId: " + toIndentedString(programVoiceId) + "\n" +
      "    instrumentId: " + toIndentedString(instrumentId) + "\n" +
      "    deltaIn: " + toIndentedString(deltaIn) + "\n" +
      "    deltaOut: " + toIndentedString(deltaOut) + "\n" +
      "    mute: " + toIndentedString(mute) + "\n" +
      "    instrumentType: " + toIndentedString(instrumentType) + "\n" +
      "    instrumentMode: " + toIndentedString(instrumentMode) + "\n" +
      "    programType: " + toIndentedString(programType) + "\n" +
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

