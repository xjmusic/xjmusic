// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.Objects;
import java.util.UUID;

public class Instrument extends Entity {
  private String config;
  private InstrumentState state;
  private String name;
  private InstrumentType type;
  private UUID userId;
  private UUID libraryId;
  private Exception stateException;
  private Exception typeException;
  private Double density;

  /**
   Create a new instrument

   @return new instrument
   */
  public static Instrument create() {
    return new Instrument()
      .setDensity(1.0)
      .setId(UUID.randomUUID());
  }

  /**
   Create a new instrument

   @param user    of instrument
   @param library of instrument
   @param type    of instrument
   @param state   of instrument
   @param name    of instrument
   @return new instrument
   */
  public static Instrument create(User user, Library library, InstrumentType type, InstrumentState state, String name) {
    return create(library)
      .setUserId(user.getId())
      .setTypeEnum(type)
      .setStateEnum(state)
      .setName(name);
  }

  /**
   Create a new instrument

   @param user    of instrument
   @param library of instrument
   @param type    of instrument
   @param state   of instrument
   @param name    of instrument
   @param density of instrument
   @return new instrument
   */
  public static Instrument create(User user, Library library, String type, String state, String name, Double density) {
    return create(library)
      .setUserId(user.getId())
      .setType(type)
      .setState(state)
      .setDensity(density)
      .setName(name);
  }

  /**
   Create a new instrument

   @param library of instrument
   @return new instrument
   */
  public static Instrument create(Library library) {
    return create()
      .setLibraryId(library.getId());
  }

  /*
  FUTURE address cloning instrument
  @Override
  public Instrument setContentCloned(Instrument of) {
    setMemes(of.getMemes());
    setAudios(of.getAudios());
    setAudioEvents(of.getAudioEvents());
    setAudioChords(of.getAudioChords());
    return this;
  }
*/

/*
FUTURE address density computation for instrument
  public Double getDensity() {
    double DL = 0;
    double L = 0;
    for (Audio audio : getAudios()) {
      DL += audio.getDensity() * audio.getLength();
      L += audio.getLength();
    }
    return 0 < L ? DL / L : 0;
  }
  */

  /**
   get Density

   @return Density
   */
  public Double getDensity() {
    return density;
  }

  /**
   get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  /**
   Get config for the instrument

   @return config
   */
  public String getConfig() {
    return config;
  }

  /**
   get LibraryId

   @return LibraryId
   */
  public UUID getLibraryId() {
    return libraryId;
  }

  /**
   get ParentId

   @return ParentId
   */
  public UUID getParentId() {
    return libraryId;
  }

  /**
   get State

   @return State
   */
  public InstrumentState getState() {
    return state;
  }

  /**
   get Type

   @return Type
   */
  public InstrumentType getType() {
    return type;
  }

  /**
   get UserId

   @return UserId
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   set Name

   @param name to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set config of instrument

   @param config to set
   @return this Instrument (for chaining methods)
   */
  public Instrument setConfig(String config) {
    this.config = config;
    return this;
  }

  /**
   set id

   @param id to set
   @return this Instrument (for chaining setters)
   */
  @Override
  public Instrument setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   set Density

   @param density to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   set LibraryId

   @param libraryId to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setLibraryId(UUID libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  /**
   set State

   @param state to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setState(String state) {
    try {
      this.state = InstrumentState.validate(state);
    } catch (ValueException e) {
      stateException = e;
    }
    return this;
  }

  /**
   set StateEnum

   @param state to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setStateEnum(InstrumentState state) {
    this.state = state;
    return this;
  }

  /**
   set Type

   @param type to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setType(String type) {
    try {
      this.type = InstrumentType.validate(type);
    } catch (ValueException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setTypeEnum(InstrumentType type) {
    this.type = type;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public String toString() {
    return name + " " + "(" + type + ")";
  }

  @Override
  public void validate() throws ValueException {
    Value.require(userId, "User ID");
    Value.require(libraryId, "Library ID");
    Value.require(name, "Name");

    Value.requireNo(typeException, "Type");
    Value.require(type, "Type");

    Value.requireNo(stateException, "State");
    Value.require(state, "State");

    if (Objects.isNull(config)) config = "";
  }

}
