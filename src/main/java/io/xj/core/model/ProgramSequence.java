// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.entity.Entity;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramSequence extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("name")
    .add("key")
    .add("density")
    .add("total")
    .add("tempo")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Program.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(ProgramSequencePattern.class)
    .add(ProgramSequenceBinding.class)
    .add(ProgramSequenceChord.class)
    .build();
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
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return RESOURCE_HAS_MANY;
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
  public void validate() throws CoreException {
    super.validate();
    require(programId, "Program ID");
    require(name, "Name");
    require(key, "Key");
    require(density, "Density");
    require(tempo, "Tempo");
  }

}
