package io.xj.model.pojos;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramSequenceBinding implements Serializable, Comparable<ProgramSequenceBinding> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private UUID programSequenceId;
  private Integer offset;

  public ProgramSequenceBinding() {
  }

  public ProgramSequenceBinding(ProgramSequenceBinding value) {
    this.id = value.id;
    this.programId = value.programId;
    this.programSequenceId = value.programSequenceId;
    this.offset = value.offset;
  }

  public ProgramSequenceBinding(
    UUID id,
    UUID programId,
    UUID programSequenceId,
    Integer offset
  ) {
    this.id = id;
    this.programId = programId;
    this.programSequenceId = programSequenceId;
    this.offset = offset;
  }

  /**
   Getter for <code>xj.program_sequence_binding.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_sequence_binding.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_sequence_binding.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_sequence_binding.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for <code>xj.program_sequence_binding.program_sequence_id</code>.
   */
  public UUID getProgramSequenceId() {
    return this.programSequenceId;
  }

  /**
   Setter for <code>xj.program_sequence_binding.program_sequence_id</code>.
   */
  public void setProgramSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
  }

  /**
   Getter for <code>xj.program_sequence_binding.offset</code>.
   */
  public Integer getOffset() {
    return this.offset;
  }

  /**
   Setter for <code>xj.program_sequence_binding.offset</code>.
   */
  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramSequenceBinding (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(programSequenceId);
    sb.append(", ").append(offset);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(ProgramSequenceBinding o) {
    if (!Objects.equals(programSequenceId, o.programSequenceId))
      return programSequenceId.compareTo(o.programSequenceId);
    return id.compareTo(o.id);
  }
}
