package io.xj.model.pojos;


import io.xj.model.enums.ProgramState;
import io.xj.model.enums.ProgramType;

import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Program implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID libraryId;
  private ProgramState state;
  private String key;
  private Float tempo;
  private ProgramType type;
  private String name;
  private String config;
  private Boolean isDeleted;
  private Long updatedAt;

  public Program() {
  }

  public Program(Program value) {
    this.id = value.id;
    this.libraryId = value.libraryId;
    this.state = value.state;
    this.key = value.key;
    this.tempo = value.tempo;
    this.type = value.type;
    this.name = value.name;
    this.config = value.config;
    this.isDeleted = value.isDeleted;
    this.updatedAt = value.updatedAt;
  }

  public Program(
    UUID id,
    UUID libraryId,
    ProgramState state,
    String key,
    Float tempo,
    ProgramType type,
    String name,
    String config,
    Boolean isDeleted,
    Long updatedAt
  ) {
    this.id = id;
    this.libraryId = libraryId;
    this.state = state;
    this.key = key;
    this.tempo = tempo;
    this.type = type;
    this.name = name;
    this.config = config;
    this.isDeleted = isDeleted;
    this.updatedAt = updatedAt;
  }

  /**
   Getter for <code>xj.program.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.program.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.program.library_id</code>.
   */
  public UUID getLibraryId() {
    return this.libraryId;
  }

  /**
   Setter for <code>xj.program.library_id</code>.
   */
  public void setLibraryId(UUID libraryId) {
    this.libraryId = libraryId;
  }

  /**
   Getter for <code>xj.program.state</code>.
   */
  public ProgramState getState() {
    return this.state;
  }

  /**
   Setter for <code>xj.program.state</code>.
   */
  public void setState(ProgramState state) {
    this.state = state;
  }

  /**
   Getter for <code>xj.program.key</code>.
   */
  public String getKey() {
    return this.key;
  }

  /**
   Setter for <code>xj.program.key</code>.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   Getter for <code>xj.program.tempo</code>.
   */
  public Float getTempo() {
    return this.tempo;
  }

  /**
   Setter for <code>xj.program.tempo</code>.
   */
  public void setTempo(Float tempo) {
    this.tempo = tempo;
  }

  /**
   Getter for <code>xj.program.type</code>.
   */
  public ProgramType getType() {
    return this.type;
  }

  /**
   Setter for <code>xj.program.type</code>.
   */
  public void setType(ProgramType type) {
    this.type = type;
  }

  /**
   Getter for <code>xj.program.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.program.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.program.config</code>.
   */
  public String getConfig() {
    return this.config;
  }

  /**
   Setter for <code>xj.program.config</code>.
   */
  public void setConfig(String config) {
    this.config = config;
  }

  /**
   Getter for <code>xj.program.is_deleted</code>.
   */
  public Boolean getIsDeleted() {
    return this.isDeleted;
  }

  /**
   Setter for <code>xj.program.is_deleted</code>.
   */
  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  /**
   Getter for <code>xj.program.updated_at</code>.
   */
  public Long getUpdatedAt() {
    return this.updatedAt;
  }

  /**
   Setter for <code>xj.program.updated_at</code>.
   */
  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Program (");

    sb.append(id);
    sb.append(", ").append(libraryId);
    sb.append(", ").append(state);
    sb.append(", ").append(key);
    sb.append(", ").append(tempo);
    sb.append(", ").append(type);
    sb.append(", ").append(name);
    sb.append(", ").append(config);
    sb.append(", ").append(isDeleted);
    sb.append(", ").append(updatedAt);

    sb.append(")");
    return sb.toString();
  }
}
