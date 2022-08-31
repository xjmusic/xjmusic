// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;
import java.util.UUID;


public class SegmentChoiceArrangementPick   {

  private @Valid UUID id;
  private @Valid UUID segmentId;
  private @Valid UUID segmentChoiceArrangementId;
  private @Valid UUID segmentChordVoicingId;
  private @Valid UUID instrumentAudioId;
  private @Valid UUID programSequencePatternEventId;
  private @Valid Double start;
  private @Valid Double length;
  private @Valid Double amplitude;
  private @Valid String tones;
  private @Valid String event;

  /**
   **/
  public SegmentChoiceArrangementPick id(UUID id) {
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
  public SegmentChoiceArrangementPick segmentId(UUID segmentId) {
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
  public SegmentChoiceArrangementPick segmentChoiceArrangementId(UUID segmentChoiceArrangementId) {
    this.segmentChoiceArrangementId = segmentChoiceArrangementId;
    return this;
  }


  @JsonProperty("segmentChoiceArrangementId")
  public UUID getSegmentChoiceArrangementId() {
    return segmentChoiceArrangementId;
  }
  public void setSegmentChoiceArrangementId(UUID segmentChoiceArrangementId) {
    this.segmentChoiceArrangementId = segmentChoiceArrangementId;
  }

  /**
   **/
  public SegmentChoiceArrangementPick segmentChordVoicingId(UUID segmentChordVoicingId) {
    this.segmentChordVoicingId = segmentChordVoicingId;
    return this;
  }


  @JsonProperty("segmentChordVoicingId")
  public UUID getSegmentChordVoicingId() {
    return segmentChordVoicingId;
  }
  public void setSegmentChordVoicingId(UUID segmentChordVoicingId) {
    this.segmentChordVoicingId = segmentChordVoicingId;
  }

  /**
   **/
  public SegmentChoiceArrangementPick instrumentAudioId(UUID instrumentAudioId) {
    this.instrumentAudioId = instrumentAudioId;
    return this;
  }


  @JsonProperty("instrumentAudioId")
  public UUID getInstrumentAudioId() {
    return instrumentAudioId;
  }
  public void setInstrumentAudioId(UUID instrumentAudioId) {
    this.instrumentAudioId = instrumentAudioId;
  }

  /**
   **/
  public SegmentChoiceArrangementPick programSequencePatternEventId(UUID programSequencePatternEventId) {
    this.programSequencePatternEventId = programSequencePatternEventId;
    return this;
  }


  @JsonProperty("programSequencePatternEventId")
  public UUID getProgramSequencePatternEventId() {
    return programSequencePatternEventId;
  }
  public void setProgramSequencePatternEventId(UUID programSequencePatternEventId) {
    this.programSequencePatternEventId = programSequencePatternEventId;
  }

  /**
   **/
  public SegmentChoiceArrangementPick start(Double start) {
    this.start = start;
    return this;
  }


  @JsonProperty("start")
  public Double getStart() {
    return start;
  }
  public void setStart(Double start) {
    this.start = start;
  }

  /**
   **/
  public SegmentChoiceArrangementPick length(Double length) {
    this.length = length;
    return this;
  }


  @JsonProperty("length")
  public Double getLength() {
    return length;
  }
  public void setLength(Double length) {
    this.length = length;
  }

  /**
   **/
  public SegmentChoiceArrangementPick amplitude(Double amplitude) {
    this.amplitude = amplitude;
    return this;
  }


  @JsonProperty("amplitude")
  public Double getAmplitude() {
    return amplitude;
  }
  public void setAmplitude(Double amplitude) {
    this.amplitude = amplitude;
  }

  /**
   **/
  public SegmentChoiceArrangementPick tones(String tones) {
    this.tones = tones;
    return this;
  }


  @JsonProperty("tones")
  public String getTones() {
    return tones;
  }
  public void setTones(String tones) {
    this.tones = tones;
  }

  /**
   **/
  public SegmentChoiceArrangementPick event(String event) {
    this.event = event;
    return this;
  }


  @JsonProperty("event")
  public String getEvent() {
    return event;
  }
  public void setEvent(String event) {
    this.event = event;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SegmentChoiceArrangementPick segmentChoiceArrangementPick = (SegmentChoiceArrangementPick) o;
    return Objects.equals(this.id, segmentChoiceArrangementPick.id) &&
        Objects.equals(this.segmentId, segmentChoiceArrangementPick.segmentId) &&
        Objects.equals(this.segmentChoiceArrangementId, segmentChoiceArrangementPick.segmentChoiceArrangementId) &&
        Objects.equals(this.segmentChordVoicingId, segmentChoiceArrangementPick.segmentChordVoicingId) &&
        Objects.equals(this.instrumentAudioId, segmentChoiceArrangementPick.instrumentAudioId) &&
        Objects.equals(this.programSequencePatternEventId, segmentChoiceArrangementPick.programSequencePatternEventId) &&
        Objects.equals(this.start, segmentChoiceArrangementPick.start) &&
        Objects.equals(this.length, segmentChoiceArrangementPick.length) &&
        Objects.equals(this.amplitude, segmentChoiceArrangementPick.amplitude) &&
        Objects.equals(this.tones, segmentChoiceArrangementPick.tones) &&
        Objects.equals(this.event, segmentChoiceArrangementPick.event);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, segmentChoiceArrangementId, segmentChordVoicingId, instrumentAudioId, programSequencePatternEventId, start, length, amplitude, tones, event);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SegmentChoiceArrangementPick {\n");

    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    segmentId: ").append(toIndentedString(segmentId)).append("\n");
    sb.append("    segmentChoiceArrangementId: ").append(toIndentedString(segmentChoiceArrangementId)).append("\n");
    sb.append("    segmentChordVoicingId: ").append(toIndentedString(segmentChordVoicingId)).append("\n");
    sb.append("    instrumentAudioId: ").append(toIndentedString(instrumentAudioId)).append("\n");
    sb.append("    programSequencePatternEventId: ").append(toIndentedString(programSequencePatternEventId)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    length: ").append(toIndentedString(length)).append("\n");
    sb.append("    amplitude: ").append(toIndentedString(amplitude)).append("\n");
    sb.append("    tones: ").append(toIndentedString(tones)).append("\n");
    sb.append("    event: ").append(toIndentedString(event)).append("\n");
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

