package io.xj.hub.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramVoiceTrack implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private UUID programVoiceId;
  private String name;
  private Float order;

  public ProgramVoiceTrack() {
  }

  public ProgramVoiceTrack(ProgramVoiceTrack value) {
    this.id = value.id;
    this.programId = value.programId;
    this.programVoiceId = value.programVoiceId;
    this.name = value.name;
    this.order = value.order;
  }

  public ProgramVoiceTrack(
    UUID id,
    UUID programId,
    UUID programVoiceId,
    String name,
    Float order
  ) {
    this.id = id;
    this.programId = programId;
    this.programVoiceId = programVoiceId;
    this.name = name;
    this.order = order;
  }

  /**
   Getter for <code>xj.program_voice_track.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_voice_track.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_voice_track.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_voice_track.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for <code>xj.program_voice_track.program_voice_id</code>.
   */
  public UUID getProgramVoiceId() {
    return this.programVoiceId;
  }

  /**
   Setter for <code>xj.program_voice_track.program_voice_id</code>.
   */
  public void setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
  }

  /**
   Getter for <code>xj.program_voice_track.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program_voice_track.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.program_voice_track.order</code>.
   */
  public Float getOrder() {
    return this.order;
  }

  /**
   Setter for <code>xj.program_voice_track.order</code>.
   */
  public void setOrder(Float order) {
    this.order = order;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramVoiceTrack (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(programVoiceId);
    sb.append(", ").append(name);
    sb.append(", ").append(order);

    sb.append(")");
    return sb.toString();
  }
}
