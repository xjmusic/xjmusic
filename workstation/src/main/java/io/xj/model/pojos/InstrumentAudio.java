package io.xj.model.pojos;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class InstrumentAudio implements Serializable, Comparable<InstrumentAudio> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID instrumentId;
  private String name;
  private String waveformKey;
  private Float transientSeconds;
  private Float lengthSeconds;
  private Float legngthSeconds;
  private Float loopBeats;
  private Float tempo;
  private Float intensity;
  private String event;
  private Float volume;
  private String tones;

  public InstrumentAudio() {
  }

  public InstrumentAudio(InstrumentAudio value) {
    this.id = value.id;
    this.instrumentId = value.instrumentId;
    this.name = value.name;
    this.waveformKey = value.waveformKey;
    this.transientSeconds = value.transientSeconds;
    this.lengthSeconds = value.lengthSeconds;
    this.loopBeats = value.loopBeats;
    this.tempo = value.tempo;
    this.intensity = value.intensity;
    this.event = value.event;
    this.volume = value.volume;
    this.tones = value.tones;
  }

  public InstrumentAudio(
    UUID id,
    UUID instrumentId,
    String name,
    String waveformKey,
    Float transientSeconds,
    Float lengthSeconds,
    Float loopBeats,
    Float tempo,
    Float intensity,
    String event,
    Float volume,
    String tones
  ) {
    this.id = id;
    this.instrumentId = instrumentId;
    this.name = name;
    this.waveformKey = waveformKey;
    this.transientSeconds = transientSeconds;
    this.lengthSeconds = lengthSeconds;
    this.loopBeats = loopBeats;
    this.tempo = tempo;
    this.intensity = intensity;
    this.event = event;
    this.volume = volume;
    this.tones = tones;
  }

  /**
   Getter for <code>xj.instrument_audio.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.instrument_audio.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.instrument_audio.instrument_id</code>.
   */
  public UUID getInstrumentId() {
    return this.instrumentId;
  }

  /**
   Setter for <code>xj.instrument_audio.instrument_id</code>.
   */
  public void setInstrumentId(UUID instrumentId) {
    this.instrumentId = instrumentId;
  }

  /**
   Getter for <code>xj.instrument_audio.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.instrument_audio.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.instrument_audio.waveform_key</code>.
   */
  public String getWaveformKey() {
    return this.waveformKey;
  }

  /**
   Setter for <code>xj.instrument_audio.waveform_key</code>.
   */
  public void setWaveformKey(String waveformKey) {
    this.waveformKey = waveformKey;
  }

  /**
   Getter for <code>xj.instrument_audio.transient_seconds</code>.
   */
  public Float getTransientSeconds() {
    return this.transientSeconds;
  }

  /**
   Setter for <code>xj.instrument_audio.transient_seconds</code>.
   */
  public void setTransientSeconds(Float transientSeconds) {
    this.transientSeconds = transientSeconds;
  }

  /**
   Getter for <code>xj.instrument_audio.length_seconds</code>.
   */
  public Float getLengthSeconds() {
    return this.lengthSeconds;
  }

  /**
   Setter for <code>xj.instrument_audio.length_seconds</code>.
   */
  public void setLengthSeconds(Float lengthSeconds) {
    this.lengthSeconds = lengthSeconds;
  }

  /**
   Getter for <code>xj.instrument_audio.loop_beats</code>.
   */
  public Float getLoopBeats() {
    return this.loopBeats;
  }

  /**
   Setter for <code>xj.instrument_audio.loop_beats</code>.
   */
  public void setLoopBeats(Float loopBeats) {
    this.loopBeats = loopBeats;
  }

  /**
   Getter for <code>xj.instrument_audio.tempo</code>.
   */
  public Float getTempo() {
    return this.tempo;
  }

  /**
   Setter for <code>xj.instrument_audio.tempo</code>.
   */
  public void setTempo(Float tempo) {
    this.tempo = tempo;
  }

  /**
   Getter for <code>xj.instrument_audio.intensity</code>.
   */
  public Float getIntensity() {
    return this.intensity;
  }

  /**
   Setter for <code>xj.instrument_audio.intensity</code>.
   */
  public void setIntensity(Float intensity) {
    this.intensity = intensity;
  }

  /**
   Getter for <code>xj.instrument_audio.event</code>.
   */
  public String getEvent() {
    return this.event;
  }

  /**
   Setter for <code>xj.instrument_audio.event</code>.
   */
  public void setEvent(String event) {
    this.event = event;
  }

  /**
   Getter for <code>xj.instrument_audio.volume</code>.
   */
  public Float getVolume() {
    return this.volume;
  }

  /**
   Setter for <code>xj.instrument_audio.volume</code>.
   */
  public void setVolume(Float volume) {
    this.volume = volume;
  }

  /**
   Getter for <code>xj.instrument_audio.tones</code>.
   */
  public String getTones() {
    return this.tones;
  }

  /**
   Setter for <code>xj.instrument_audio.tones</code>.
   */
  public void setTones(String tones) {
    this.tones = tones;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("InstrumentAudio (");

    sb.append(id);
    sb.append(", ").append(instrumentId);
    sb.append(", ").append(name);
    sb.append(", ").append(waveformKey);
    sb.append(", ").append(transientSeconds);
    sb.append(", ").append(lengthSeconds);
    sb.append(", ").append(loopBeats);
    sb.append(", ").append(tempo);
    sb.append(", ").append(intensity);
    sb.append(", ").append(event);
    sb.append(", ").append(volume);
    sb.append(", ").append(tones);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(InstrumentAudio o) {
    if (!Objects.equals(name, o.name))
      return name.compareTo(o.name);
    return id.compareTo(o.id);
  }
}
