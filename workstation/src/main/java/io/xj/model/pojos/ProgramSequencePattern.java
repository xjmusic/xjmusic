package io.xj.model.pojos;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramSequencePattern implements Serializable, Comparable<ProgramSequencePattern> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private UUID programSequenceId;
  private UUID programVoiceId;
  private String name;
  private Short total;

  public ProgramSequencePattern() {
  }

  public ProgramSequencePattern(ProgramSequencePattern value) {
    this.id = value.id;
    this.programId = value.programId;
    this.programSequenceId = value.programSequenceId;
    this.programVoiceId = value.programVoiceId;
    this.name = value.name;
    this.total = value.total;
  }

  public ProgramSequencePattern(
    UUID id,
    UUID programId,
    UUID programSequenceId,
    UUID programVoiceId,
    String name,
    Short total
  ) {
    this.id = id;
    this.programId = programId;
    this.programSequenceId = programSequenceId;
    this.programVoiceId = programVoiceId;
    this.name = name;
    this.total = total;
  }

  /**
   Getter for <code>xj.program_sequence_pattern.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_sequence_pattern.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_sequence_pattern.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_sequence_pattern.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for <code>xj.program_sequence_pattern.program_sequence_id</code>.
   */
  public UUID getProgramSequenceId() {
    return this.programSequenceId;
  }

  /**
   Setter for <code>xj.program_sequence_pattern.program_sequence_id</code>.
   */
  public void setProgramSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
  }

  /**
   Getter for <code>xj.program_sequence_pattern.program_voice_id</code>.
   */
  public UUID getProgramVoiceId() {
    return this.programVoiceId;
  }

  /**
   Setter for <code>xj.program_sequence_pattern.program_voice_id</code>.
   */
  public void setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
  }

  /**
   Getter for <code>xj.program_sequence_pattern.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program_sequence_pattern.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.program_sequence_pattern.total</code>.
   */
  public Short getTotal() {
    return this.total;
  }

  /**
   Setter for <code>xj.program_sequence_pattern.total</code>.
   */
  public void setTotal(Short total) {
    this.total = total;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramSequencePattern (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(programSequenceId);
    sb.append(", ").append(programVoiceId);
    sb.append(", ").append(name);
    sb.append(", ").append(total);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(ProgramSequencePattern o) {
    if (!Objects.equals(name, o.name))
      return name.compareTo(o.name);
    return id.compareTo(o.id);
  }
}
