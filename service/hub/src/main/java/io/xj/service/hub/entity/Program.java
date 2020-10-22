// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Program extends Entity {
  private UUID userId;
  private UUID libraryId;
  private ProgramState state;
  private String key;
  private String config;
  private Double tempo;
  private ProgramType type;
  private String name;
  private Exception stateException;
  private Exception typeException;
  private Double density;

  /**
   Create a new Program

   @return new Program
   */
  public static Program create() {
    return new Program()
      .setId(UUID.randomUUID());
  }

  /**
   Create a new Program

   @param user    of program
   @param library of program
   @param type    of program
   @param state   of program
   @param name    of program
   @param key     of program
   @param tempo   of program
   @param density of program
   @return new program
   */
  public static Program create(User user, Library library, ProgramType type, ProgramState state, String name, String key, double tempo, double density) {
    return create()
      .setUserId(user.getId())
      .setLibraryId(library.getId())
      .setTypeEnum(type)
      .setStateEnum(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density);
  }

  /**
   Create a new Program

   @param user    of program
   @param library of program
   @param type    of program
   @param state   of program
   @param name    of program
   @param key     of program
   @param tempo   of program
   @param density of program
   @return new program
   */
  public static Program create(User user, Library library, String type, String state, String name, String key, double tempo, double density) {
    return create()
      .setUserId(user.getId())
      .setLibraryId(library.getId())
      .setType(type)
      .setState(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density);
  }

  /**
   Get key for the program

   @return key
   */
  public String getKey() {
    return key;
  }

  /**
   Get Library ID

   @return library id
   */
  public UUID getLibraryId() {
    return libraryId;
  }

  /**
   Get name for the program

   @return name
   */
  public String getName() {
    return name;
  }

  /**
   Get config for the program

   @return config
   */
  public String getConfig() {
    return config;
  }

  /**
   Get state of Program

   @return state
   */
  public ProgramState getState() {
    return state;
  }

  /**
   Get tempo of program

   @return tempo
   */
  public Double getTempo() {
    return tempo;
  }

  /**
   Get Type

   @return Type
   */
  public ProgramType getType() {
    return type;
  }

  /**
   Get User

   @return User
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Program setCreatedAt(String createdAt) {
    super.setCreatedAt(createdAt);
    return this;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Program setCreatedAtInstant(Instant createdAt) {
    super.setCreatedAtInstant(createdAt);
    return this;
  }

  /**
   Set the id for the program

   @param id to set
   @return this Program (for chaining methods)
   */
  @Override
  public Program setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   Set the density for the program

   @param density to set
   @return this Program (for chaining methods)
   */
  public Program setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set the key for the program

   @param key to set
   @return this Program (for chaining methods)
   */
  public Program setKey(String key) {
    this.key = key;
    return this;
  }

  /**
   Set Library ID

   @param libraryId to set
   @return this Program (for chaining methods)
   */
  public Program setLibraryId(UUID libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  /**
   Set name of program

   @param name to set
   @return this Program (for chaining methods)
   */
  public Program setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set config of program

   @param config to set
   @return this Program (for chaining methods)
   */
  public Program setConfig(String config) {
    this.config = config;
    return this;
  }

  /**
   Set state of Program

   @param value to set
   @return this Program (for chaining methods)
   */
  public Program setState(String value) {
    try {
      state = ProgramState.validate(value);
    } catch (ValueException e) {
      stateException = e;
    }
    return this;
  }

  /**
   Set state of Program

   @param value to set
   @return this Program (for chaining methods)
   */
  public Program setStateEnum(ProgramState value) {
    state = value;
    return this;
  }

  /**
   Set tempo of program

   @param tempo to set
   @return this Program (for chaining methods)
   */
  public Program setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Program (for chaining methods)
   */
  public Program setType(String type) {
    try {
      this.type = ProgramType.validate(type);
    } catch (ValueException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set TypeEnum

   @param type to set
   @return this Program (for chaining methods)
   */
  public Program setTypeEnum(ProgramType type) {
    this.type = type;
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Program setUpdatedAt(String updatedAt) {
    super.setUpdatedAt(updatedAt);
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Program setUpdatedAtInstant(Instant updatedAt) {
    super.setUpdatedAtInstant(updatedAt);
    return this;
  }

  /**
   Set User ID

   @param userId to set
   @return this Program (for chaining methods)
   */
  public Program setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   Validate data.

   @throws ValueException if invalid.
   */
  public void validate() throws ValueException {
    Value.require(userId, "User ID");
    Value.require(libraryId, "Library ID");
    Value.require(name, "Name");
    Value.require(key, "Key");
    Value.requireNonZero(tempo, "Tempo");

    Value.requireNo(typeException, "Type");
    Value.require(type, "Type");

    Value.requireNo(stateException, "State");
    Value.require(state, "State");

    if (Objects.isNull(config)) config = "";
  }

  public Double getDensity() {
    return density;
  }

}
