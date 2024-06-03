// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.model.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;


public class SegmentChoiceArrangementPick {

  UUID id;
  int segmentId;
  UUID segmentChoiceArrangementId;
  UUID segmentChordVoicingId;
  UUID instrumentAudioId;
  UUID programSequencePatternEventId;
  long startAtSegmentMicros;
  long lengthMicros;
  float amplitude;
  std::string tones;
  std::string event;

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
  public SegmentChoiceArrangementPick segmentId(int segmentId) {
    this.segmentId = segmentId;
    return this;
  }


  @JsonProperty("segmentId")
  public int getSegmentId() {
    return segmentId;
  }

  public void setSegmentId(int segmentId) {
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
  public SegmentChoiceArrangementPick startAtSegmentMicros(long segmentMicros) {
    this.startAtSegmentMicros = segmentMicros;
    return this;
  }


  @JsonProperty("startAtSegmentMicros")
  public long getStartAtSegmentMicros() {
    return startAtSegmentMicros;
  }

  public void setStartAtSegmentMicros(long segmentMicros) {
    this.startAtSegmentMicros = segmentMicros;
  }

  /**
   **/
  public SegmentChoiceArrangementPick lengthMicros(long micros) {
    this.lengthMicros = micros;
    return this;
  }


  @JsonProperty("lengthMicros")
  public long getLengthMicros() {
    return lengthMicros;
  }

  public void setLengthMicros(long micros) {
    this.lengthMicros = micros;
  }

  /**
   **/
  public SegmentChoiceArrangementPick amplitude(float amplitude) {
    this.amplitude = amplitude;
    return this;
  }


  @JsonProperty("amplitude")
  public float getAmplitude() {
    return amplitude;
  }

  public void setAmplitude(float amplitude) {
    this.amplitude = amplitude;
  }

  /**
   **/
  public SegmentChoiceArrangementPick tones(std::string tones) {
    this.tones = tones;
    return this;
  }


  @JsonProperty("tones")
  public std::string getTones() {
    return tones;
  }

  public void setTones(std::string tones) {
    this.tones = tones;
  }

  /**
   **/
  public SegmentChoiceArrangementPick event(std::string event) {
    this.event = event;
    return this;
  }


  @JsonProperty("event")
  public std::string getEvent() {
    return event;
  }

  public void setEvent(std::string event) {
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
      Objects.equals(this.startAtSegmentMicros, segmentChoiceArrangementPick.startAtSegmentMicros) &&
      Objects.equals(this.lengthMicros, segmentChoiceArrangementPick.lengthMicros) &&
      Objects.equals(this.amplitude, segmentChoiceArrangementPick.amplitude) &&
      Objects.equals(this.tones, segmentChoiceArrangementPick.tones) &&
      Objects.equals(this.event, segmentChoiceArrangementPick.event);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, segmentId, segmentChoiceArrangementId, segmentChordVoicingId, instrumentAudioId, programSequencePatternEventId, startAtSegmentMicros, lengthMicros, amplitude, tones, event);
  }

  @Override
  public std::string toString() {

    return "class SegmentChoiceArrangementPick {\n" +
      "    id: " + toIndentedString(id) + "\n" +
      "    segmentId: " + toIndentedString(segmentId) + "\n" +
      "    segmentChoiceArrangementId: " + toIndentedString(segmentChoiceArrangementId) + "\n" +
      "    segmentChordVoicingId: " + toIndentedString(segmentChordVoicingId) + "\n" +
      "    instrumentAudioId: " + toIndentedString(instrumentAudioId) + "\n" +
      "    programSequencePatternEventId: " + toIndentedString(programSequencePatternEventId) + "\n" +
      "    startAtSegmentMicros: " + toIndentedString(startAtSegmentMicros) + "\n" +
      "    lengthMicros: " + toIndentedString(lengthMicros) + "\n" +
      "    amplitude: " + toIndentedString(amplitude) + "\n" +
      "    tones: " + toIndentedString(tones) + "\n" +
      "    event: " + toIndentedString(event) + "\n" +
      "}";
  }

  /**
   Convert the given object to string with each line indented by 4 spaces
   (except the first line).
   */
  std::string toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

