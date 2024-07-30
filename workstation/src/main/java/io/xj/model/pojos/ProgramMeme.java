package io.xj.model.pojos;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramMeme implements Serializable, Comparable<ProgramMeme> {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private String name;
  private UUID programId;

  public ProgramMeme() {
  }

  public ProgramMeme(ProgramMeme value) {
    this.id = value.id;
    this.name = value.name;
    this.programId = value.programId;
  }

  public ProgramMeme(
    UUID id,
    String name,
    UUID programId
  ) {
    this.id = id;
    this.name = name;
    this.programId = programId;
  }

  /**
   Getter for <code>xj.program_meme.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_meme.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_meme.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program_meme.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.program_meme.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_meme.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramMeme (");

    sb.append(id);
    sb.append(", ").append(name);
    sb.append(", ").append(programId);

    sb.append(")");
    return sb.toString();
  }

  @Override
  public int compareTo(ProgramMeme o) {
    if (!Objects.equals(name, o.name))
      return name.compareTo(o.name);
    return id.compareTo(o.id);
  }
}
