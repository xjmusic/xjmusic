package io.xj.model.pojos;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramSequenceChordVoicing implements Serializable, Comparable<ProgramSequenceChordVoicing> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private UUID programSequenceChordId;
  private String notes;
  private UUID programVoiceId;

  public ProgramSequenceChordVoicing() {
  }

  public ProgramSequenceChordVoicing(ProgramSequenceChordVoicing value) {
    this.id = value.id;
    this.programId = value.programId;
    this.programSequenceChordId = value.programSequenceChordId;
    this.notes = value.notes;
    this.programVoiceId = value.programVoiceId;
  }

  public ProgramSequenceChordVoicing(
    UUID id,
    UUID programId,
    UUID programSequenceChordId,
    String notes,
    UUID programVoiceId
  ) {
    this.id = id;
    this.programId = programId;
    this.programSequenceChordId = programSequenceChordId;
    this.notes = notes;
    this.programVoiceId = programVoiceId;
  }

  /**
   Getter for <code>xj.program_sequence_chord_voicing.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_sequence_chord_voicing.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_sequence_chord_voicing.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_sequence_chord_voicing.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for
   <code>xj.program_sequence_chord_voicing.program_sequence_chord_id</code>.
   */
  public UUID getProgramSequenceChordId() {
    return this.programSequenceChordId;
  }

  /**
   Setter for
   <code>xj.program_sequence_chord_voicing.program_sequence_chord_id</code>.
   */
  public void setProgramSequenceChordId(UUID programSequenceChordId) {
    this.programSequenceChordId = programSequenceChordId;
  }

  /**
   Getter for <code>xj.program_sequence_chord_voicing.notes</code>.
   */
  public String getNotes() {
    return this.notes;
  }

  /**
   Setter for <code>xj.program_sequence_chord_voicing.notes</code>.
   */
  public void setNotes(String notes) {
    this.notes = notes;
  }

  /**
   Getter for
   <code>xj.program_sequence_chord_voicing.program_voice_id</code>.
   */
  public UUID getProgramVoiceId() {
    return this.programVoiceId;
  }

  /**
   Setter for
   <code>xj.program_sequence_chord_voicing.program_voice_id</code>.
   */
  public void setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramSequenceChordVoicing (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(programSequenceChordId);
    sb.append(", ").append(notes);
    sb.append(", ").append(programVoiceId);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(ProgramSequenceChordVoicing o) {
    if (!Objects.equals(programSequenceChordId, o.programSequenceChordId))
      return programSequenceChordId.compareTo(o.programSequenceChordId);
    if (!Objects.equals(programVoiceId, o.programVoiceId))
      return programVoiceId.compareTo(o.programVoiceId);
    return id.compareTo(o.id);
  }
}
