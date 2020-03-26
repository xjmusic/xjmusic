// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramSequencePattern extends Entity {
  private UUID programId;
  private UUID programSequenceId;
  private UUID programVoiceId;
  private ProgramSequencePatternType type;
  private Integer total;
  private String name;
  private Exception typeException;

  /**
   Create a new Pattern

   @return new pattern
   */
  public static ProgramSequencePattern create() {
    return new ProgramSequencePattern().setId(UUID.randomUUID());
  }

  /**
   Create a new Pattern

   @param sequence of pattern
   @param voice    of pattern
   @param type     of pattern
   @param total    beats of pattern
   @param name     of pattern
   @return new pattern
   */
  public static ProgramSequencePattern create(ProgramSequence sequence, ProgramVoice voice, ProgramSequencePatternType type, int total, String name) {
    return create()
      .setProgramId(sequence.getProgramId())
      .setSequence(sequence)
      .setVoice(voice)
      .setTypeEnum(type)
      .setTotal(total)
      .setName(name);
  }

  /**
   Create a new Pattern

   @param sequence of pattern
   @param voice    of pattern
   @param type     of pattern
   @param total    beats of pattern
   @param name     of pattern
   @return new pattern
   */
  public static ProgramSequencePattern create(ProgramSequence sequence, ProgramVoice voice, String type, int total, String name) {
    return create(sequence, voice, ProgramSequencePatternType.valueOf(type), total, name);
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
  public ProgramSequencePattern setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  @Override
  public ProgramSequencePattern setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   Get Sequence UUID

   @return sequence id
   */
  public UUID getProgramSequenceId() {
    return programSequenceId;
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
  public ProgramSequencePatternType getType() {
    return type;
  }

  /**
   Get Voice ID

   @return voice id
   */
  public UUID getProgramVoiceId() {
    return programVoiceId;
  }

  /**
   Set Name

   @param name to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set sequence UUID by providing a Sequence entity

   @param sequence id to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setSequence(ProgramSequence sequence) {
    setProgramSequenceId(sequence.getId());
    return this;
  }

  /**
   Set Sequence UUID

   @param programSequenceId to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setProgramSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
    return this;
  }

  /**
   Set Total

   @param total to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setTotal(Integer total) {
    this.total = total;
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setType(String type) {
    try {
      this.type = ProgramSequencePatternType.validate(type);
    } catch (ValueException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setTypeEnum(ProgramSequencePatternType type) {
    this.type = type;
    return this;
  }

  /**
   Set voice UUID by providing a Voice entity

   @param voice id to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setVoice(ProgramVoice voice) {
    setProgramVoiceId(voice.getId());
    return this;
  }

  /**
   Set Voice ID

   @param programVoiceId to set
   @return this Pattern (for chaining setters)
   */
  public ProgramSequencePattern setProgramVoiceId(UUID programVoiceId) {
    this.programVoiceId = programVoiceId;
    return this;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();
    Value.require(programId, "Program ID");
    Value.require(programVoiceId, "Voice ID");
    Value.require(programSequenceId, "Sequence ID");
    Value.require(name, "Name");
    Value.require(total, "Total");

    Value.requireNo(typeException, "Type");
    Value.require(type, "Type");
  }

  /**
   @return String representation of Pattern
   */
  public String toString() {
    return String.format("%s (%s)", name, type.toString());
  }
}
