package io.xj.model.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramSequenceBindingMeme implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private UUID programSequenceBindingId;
  private String name;

  public ProgramSequenceBindingMeme() {
  }

  public ProgramSequenceBindingMeme(ProgramSequenceBindingMeme value) {
    this.id = value.id;
    this.programId = value.programId;
    this.programSequenceBindingId = value.programSequenceBindingId;
    this.name = value.name;
  }

  public ProgramSequenceBindingMeme(
    UUID id,
    UUID programId,
    UUID programSequenceBindingId,
    String name
  ) {
    this.id = id;
    this.programId = programId;
    this.programSequenceBindingId = programSequenceBindingId;
    this.name = name;
  }

  /**
   Getter for <code>xj.program_sequence_binding_meme.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_sequence_binding_meme.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_sequence_binding_meme.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_sequence_binding_meme.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for
   <code>xj.program_sequence_binding_meme.program_sequence_binding_id</code>.
   */
  public UUID getProgramSequenceBindingId() {
    return this.programSequenceBindingId;
  }

  /**
   Setter for
   <code>xj.program_sequence_binding_meme.program_sequence_binding_id</code>.
   */
  public void setProgramSequenceBindingId(UUID programSequenceBindingId) {
    this.programSequenceBindingId = programSequenceBindingId;
  }

  /**
   Getter for <code>xj.program_sequence_binding_meme.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program_sequence_binding_meme.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramSequenceBindingMeme (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(programSequenceBindingId);
    sb.append(", ").append(name);

    sb.append(")");
    return sb.toString();
  }
}
