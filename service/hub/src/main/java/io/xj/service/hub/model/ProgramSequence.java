// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

import java.util.Objects;
import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramSequence extends Entity {


  private UUID programId;
  private String name;
  private String key;
  private Double density;
  private Integer total;
  private Double tempo;

  /**
   Create a new sequence

   @param total   beats in sequence
   @param name    of sequence
   @param density of sequence
   @param key     of sequence
   @param tempo   of sequence
   @return new sequence
   */
  public static ProgramSequence create(Program program, int total, String name, double density, String key, double tempo) {
    return create()
      .setProgramId(program.getId())
      .setTotal(total)
      .setName(name)
      .setDensity(density)
      .setKey(key)
      .setTempo(tempo);
  }

  /**
   Create a new sequence

   @return new sequence
   */
  public static ProgramSequence create() {
    return new ProgramSequence().setId(UUID.randomUUID());
  }

  @Override
  public UUID getParentId() {
    return programId;
  }

  /**
   Get id of Program to which this entity belongs

   @return program id
   */
  public UUID getProgramId() {
    return programId;
  }

  /**
   Set id of Program to which this entity belongs

   @param programId to which this entity belongs
   @return this Program Entity (for chaining setters)
   */
  public ProgramSequence setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get Density

   @return density
   */
  public Double getDensity() {
    return density;
  }

  /**
   Get Key

   @return key
   */
  public String getKey() {
    return key;
  }

  /**
   Get name

   @return name
   */
  public String getName() {
    return name;
  }

  @Override
  public ProgramSequence setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   Get tempo

   @return tempo
   */
  public Double getTempo() {
    return tempo;
  }

  /**
   Get total beats

   @return total beats
   */
  public Integer getTotal() {
    return total;
  }

  /**
   Set density

   @param density to set
   @return this Sequence (for chaining setters)
   */
  public ProgramSequence setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set Key

   @param key to set
   @return this Sequence (for chaining setters)
   */
  public ProgramSequence setKey(String key) {
    this.key = key;
    return this;
  }

  /**
   Set name

   @param name to set
   @return this Sequence (for chaining setters)
   */
  public ProgramSequence setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set Tempo

   @param tempo to set
   @return this Sequence (for chaining setters)
   */
  public ProgramSequence setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set Total beats

   @param total to set
   @return this Sequence (for chaining setters)
   */
  public ProgramSequence setTotal(Integer total) {
    this.total = total;
    return this;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();
    Value.require(programId, "Program ID");
    Value.require(name, "Name");
    Value.require(key, "Key");
    Value.require(density, "Density");
    if (Objects.isNull(total)) total = 0;
    Value.require(tempo, "Tempo");
  }

}
