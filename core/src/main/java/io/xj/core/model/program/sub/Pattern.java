//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.PatternType;
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
public class Pattern extends ProgramSubEntity {
  private PatternType type;
  private UUID sequenceId;
  private Integer total;
  private UUID voiceId;
  private String name;
  private Exception typeException;

  /**
   Get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("type")
      .add("total")
      .add("name")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Sequence.class)
      .add(Voice.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(PatternEvent.class)
      .build();
  }

  /**
   Get Sequence UUID

   @return sequence id
   */
  public UUID getSequenceId() {
    return sequenceId;
  }

  /**
   Get Total beats

   @return total beats
   */
  public Integer getTotal() {
    return total;
  }

  /**
   Get type

   @return type
   */
  public PatternType getType() {
    return type;
  }

  /**
   Get Voice UUID

   @return voice id
   */
  public UUID getVoiceId() {
    return voiceId;
  }

  /**
   Set Name

   @param name to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set sequence UUID by providing a Sequence entity

   @param sequence id to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setSequence(Sequence sequence) {
    setSequenceId(sequence.getId());
    return this;
  }

  /**
   Set Sequence UUID

   @param sequenceId to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setSequenceId(UUID sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  /**
   Set Total

   @param total to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setTotal(Integer total) {
    this.total = total;
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setType(String type) {
    try {
      this.type = PatternType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setTypeEnum(PatternType type) {
    this.type = type;
    return this;
  }

  /**
   Set voice UUID by providing a Voice entity

   @param voice id to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setVoice(Voice voice) {
    setVoiceId(voice.getId());
    return this;
  }

  /**
   Set Voice UUID

   @param voiceId to set
   @return this Pattern (for chaining setters)
   */
  public Pattern setVoiceId(UUID voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  @Override
  public Pattern setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public Pattern validate() throws CoreException {
    super.validate();
    require(voiceId, "Voice ID");
    require(sequenceId, "Sequence ID");
    require(name, "Name");

    requireNo(typeException, "Type");
    require(type, "Type");

    return this;
  }

  /**
   @return String representation of Pattern
   */
  public String toString() {
    return String.format("%s (%s)", name, type.toString());
  }
}
