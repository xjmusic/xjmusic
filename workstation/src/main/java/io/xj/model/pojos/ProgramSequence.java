package io.xj.model.pojos;


import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramSequence implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private String name;
  private String key;
  private Float intensity;
  private Short total;

  public ProgramSequence() {
  }

  public ProgramSequence(ProgramSequence value) {
    this.id = value.id;
    this.programId = value.programId;
    this.name = value.name;
    this.key = value.key;
    this.intensity = value.intensity;
    this.total = value.total;
  }

  public ProgramSequence(
    UUID id,
    UUID programId,
    String name,
    String key,
    Float intensity,
    Short total
  ) {
    this.id = id;
    this.programId = programId;
    this.name = name;
    this.key = key;
    this.intensity = intensity;
    this.total = total;
  }

  /**
   Getter for <code>xj.program_sequence.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_sequence.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_sequence.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_sequence.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for <code>xj.program_sequence.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program_sequence.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.program_sequence.key</code>.
   */
  public String getKey() {
    return this.key;
  }

  /**
   Setter for <code>xj.program_sequence.key</code>.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   Getter for <code>xj.program_sequence.intensity</code>.
   */
  public Float getIntensity() {
    return this.intensity;
  }

  /**
   Setter for <code>xj.program_sequence.intensity</code>.
   */
  public void setIntensity(Float intensity) {
    this.intensity = intensity;
  }

  /**
   Getter for <code>xj.program_sequence.total</code>.
   */
  public Short getTotal() {
    return this.total;
  }

  /**
   Setter for <code>xj.program_sequence.total</code>.
   */
  public void setTotal(Short total) {
    this.total = total;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramSequence (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(name);
    sb.append(", ").append(key);
    sb.append(", ").append(intensity);
    sb.append(", ").append(total);

    sb.append(")");
    return sb.toString();
  }
}
