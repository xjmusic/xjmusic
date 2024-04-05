package io.xj.hub.pojos;


import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;

import java.io.Serializable;
import java.util.UUID;


@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class Instrument implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private UUID libraryId;
  private InstrumentType type;
  private InstrumentState state;
  private String name;
  private String config;
  private Boolean isDeleted;
  private Float volume;
  private InstrumentMode mode;
  private Long updatedAt;

  public Instrument() {
  }

  public Instrument(Instrument value) {
    this.id = value.id;
    this.libraryId = value.libraryId;
    this.type = value.type;
    this.state = value.state;
    this.name = value.name;
    this.config = value.config;
    this.isDeleted = value.isDeleted;
    this.volume = value.volume;
    this.mode = value.mode;
    this.updatedAt = value.updatedAt;
  }

  public Instrument(
    UUID id,
    UUID libraryId,
    InstrumentType type,
    InstrumentState state,
    String name,
    String config,
    Boolean isDeleted,
    Float volume,
    InstrumentMode mode,
    Long updatedAt
  ) {
    this.id = id;
    this.libraryId = libraryId;
    this.type = type;
    this.state = state;
    this.name = name;
    this.config = config;
    this.isDeleted = isDeleted;
    this.volume = volume;
    this.mode = mode;
    this.updatedAt = updatedAt;
  }

  /**
   Getter for <code>xj.instrument.id</code>.
   */
  public UUID getId() {
    return this.id;
  }

  /**
   Setter for <code>xj.instrument.id</code>.
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   Getter for <code>xj.instrument.library_id</code>.
   */
  public UUID getLibraryId() {
    return this.libraryId;
  }

  /**
   Setter for <code>xj.instrument.library_id</code>.
   */
  public void setLibraryId(UUID libraryId) {
    this.libraryId = libraryId;
  }

  /**
   Getter for <code>xj.instrument.type</code>.
   */
  public InstrumentType getType() {
    return this.type;
  }

  /**
   Setter for <code>xj.instrument.type</code>.
   */
  public void setType(InstrumentType type) {
    this.type = type;
  }

  /**
   Getter for <code>xj.instrument.state</code>.
   */
  public InstrumentState getState() {
    return this.state;
  }

  /**
   Setter for <code>xj.instrument.state</code>.
   */
  public void setState(InstrumentState state) {
    this.state = state;
  }

  /**
   Getter for <code>xj.instrument.name</code>.
   */
  public String getName() {
    return this.name;
  }

  /**
   Setter for <code>xj.instrument.name</code>.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   Getter for <code>xj.instrument.config</code>.
   */
  public String getConfig() {
    return this.config;
  }

  /**
   Setter for <code>xj.instrument.config</code>.
   */
  public void setConfig(String config) {
    this.config = config;
  }

  /**
   Getter for <code>xj.instrument.is_deleted</code>.
   */
  public Boolean getIsDeleted() {
    return this.isDeleted;
  }

  /**
   Setter for <code>xj.instrument.is_deleted</code>.
   */
  public void setIsDeleted(Boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  /**
   Getter for <code>xj.instrument.volume</code>.
   */
  public Float getVolume() {
    return this.volume;
  }

  /**
   Setter for <code>xj.instrument.volume</code>.
   */
  public void setVolume(Float volume) {
    this.volume = volume;
  }

  /**
   Getter for <code>xj.instrument.mode</code>.
   */
  public InstrumentMode getMode() {
    return this.mode;
  }

  /**
   Setter for <code>xj.instrument.mode</code>.
   */
  public void setMode(InstrumentMode mode) {
    this.mode = mode;
  }

  /**
   Getter for <code>xj.instrument.updated_at</code>.
   */
  public Long getUpdatedAt() {
    return this.updatedAt;
  }

  /**
   Setter for <code>xj.instrument.updated_at</code>.
   */
  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Instrument (");

    sb.append(id);
    sb.append(", ").append(libraryId);
    sb.append(", ").append(type);
    sb.append(", ").append(state);
    sb.append(", ").append(name);
    sb.append(", ").append(config);
    sb.append(", ").append(isDeleted);
    sb.append(", ").append(volume);
    sb.append(", ").append(mode);
    sb.append(", ").append(updatedAt);

    sb.append(")");
    return sb.toString();
  }
}
