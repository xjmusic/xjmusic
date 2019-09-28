// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.program.impl.ProgramSubEntity;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Sequence extends ProgramSubEntity {
  private String name;
  private String key;
  private Double density;
  private Integer total;
  private Double tempo;

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
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .add("key")
      .add("density")
      .add("total")
      .add("tempo")
      .build();
  }

  @Override
  public Sequence setId(UUID id) {
    super.setId(id);
    return this;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Pattern.class)
      .add(SequenceBinding.class)
      .add(SequenceChord.class)
      .build();
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
  public Sequence setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set Key

   @param key to set
   @return this Sequence (for chaining setters)
   */
  public Sequence setKey(String key) {
    this.key = key;
    return this;
  }

  /**
   Set name

   @param name to set
   @return this Sequence (for chaining setters)
   */
  public Sequence setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set Tempo

   @param tempo to set
   @return this Sequence (for chaining setters)
   */
  public Sequence setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set Total beats

   @param total to set
   @return this Sequence (for chaining setters)
   */
  public Sequence setTotal(Integer total) {
    this.total = total;
    return this;
  }

  @Override
  public Sequence setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public Sequence validate() throws CoreException {
    super.validate();
    require(name, "Name");
    require(key, "Key");
    require(density, "Density");
    require(tempo, "Tempo");
    return this;
  }

}
