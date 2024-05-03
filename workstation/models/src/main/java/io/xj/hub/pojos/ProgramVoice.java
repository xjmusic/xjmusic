package io.xj.hub.pojos;


import io.xj.hub.enums.InstrumentType;

import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ProgramVoice implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID programId;
  private InstrumentType type;
  private String name;
  private Float order;

  public ProgramVoice() {
  }

  public ProgramVoice(ProgramVoice value) {
    this.id = value.id;
    this.programId = value.programId;
    this.type = value.type;
    this.name = value.name;
    this.order = value.order;
  }

  public ProgramVoice(
    UUID id,
    UUID programId,
    InstrumentType type,
    String name,
    Float order
  ) {
    this.id = id;
    this.programId = programId;
    this.type = type;
    this.name = name;
    this.order = order;
  }

  /**
   Getter for <code>xj.program_voice.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program_voice.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program_voice.program_id</code>.
   */
  public UUID getProgramId() {
    return this.programId;
  }

  /**
   Setter for <code>xj.program_voice.program_id</code>.
   */
  public void setProgramId(UUID programId) {
    this.programId = programId;
  }

  /**
   Getter for <code>xj.program_voice.type</code>.
   */
  public InstrumentType getType() {
    return this.type;
  }

  /**
   Setter for <code>xj.program_voice.type</code>.
   */
  public void setType(InstrumentType type) {
    this.type = type;
  }

  /**
   Getter for <code>xj.program_voice.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program_voice.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.program_voice.order</code>.
   */
  public Float getOrder() {
    return this.order;
  }

  /**
   Setter for <code>xj.program_voice.order</code>.
   */
  public void setOrder(Float order) {
    this.order = order;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProgramVoice (");

    sb.append(id);
    sb.append(", ").append(programId);
    sb.append(", ").append(type);
    sb.append(", ").append(name);
    sb.append(", ").append(order);

    sb.append(")");
    return sb.toString();
  }
}
