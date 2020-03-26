// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.ChordEntity;

import java.util.UUID;

/**
 [#166690830] Pattern model handles all of its own entities
 */
public class ProgramSequenceChord extends ChordEntity {


  private UUID programId;
  private UUID programSequenceId;


  public ProgramSequenceChord() {
  }

  public ProgramSequenceChord(ProgramSequence programSequence) {
    setProgramSequence(programSequence);
  }

  /**
   Get a new SequenceChord of a specified name

   @param name to get sequenceChord of
   @return new sequenceChord
   */
  public static ProgramSequenceChord of(String name) {
    return new ProgramSequenceChord().setName(name);
  }

  /**
   Create a new SequenceChord

   @return new sequence chord
   */
  public static ProgramSequenceChord create() {
    return new ProgramSequenceChord().setId(UUID.randomUUID());
  }

  /**
   Create a new SequenceChord

   @param sequence to of chord in
   @param position of chord
   @param name     of chord
   @return new sequence chord
   */
  public static ProgramSequenceChord create(ProgramSequence sequence, double position, String name) {
    return create()
      .setProgramId(sequence.getProgramId())
      .setProgramSequenceId(sequence.getId())
      .setPosition(position)
      .setName(name);
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
  public ProgramSequenceChord setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get Sequence id

   @return sequence id
   */
  public UUID getProgramSequenceId() {
    return programSequenceId;
  }

  @Override
  public Boolean isChord() {
    return !isNoChord();
  }

  @Override
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  @Override
  public ProgramSequenceChord setName(String name) {
    super.setName(name);
    return this;
  }

  @Override
  public ProgramSequenceChord setPosition(Double position) {
    super.setPosition(position);
    return this;
  }

  /**
   Set sequence id by providing the parent sequence

   @param programSequence of which to set sequence id
   @return this SequenceChord (for chaining methods)
   */
  public ProgramSequenceChord setProgramSequence(ProgramSequence programSequence) {
    return setProgramSequenceId(programSequence.getId())
      .setProgramId(programSequence.getProgramId());
  }

  /**
   Set sequence id

   @param programSequenceId to set
   @return this SequenceChord (for chaining methods)
   */
  public ProgramSequenceChord setProgramSequenceId(UUID programSequenceId) {
    this.programSequenceId = programSequenceId;
    return this;
  }

  @Override
  public ProgramSequenceChord setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public void validate() throws ValueException {
    super.validate();
    Value.require(programId, "Program ID");
    Value.require(programSequenceId, "Sequence ID");
    ChordEntity.validate(this);
  }
}
