//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Chord;
import io.xj.core.model.program.impl.ProgramSubEntity;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166690830] Pattern model handles all of its own entities
 */
public class SequenceChord extends ProgramSubEntity implements Chord<SequenceChord> {
  private UUID sequenceId;
  private String name;
  private Double position;

  /**
   Get a new SequenceChord of a specified name

   @param name to get sequenceChord of
   @return new sequenceChord
   */
  public static SequenceChord of(String name) {
    return new SequenceChord().setName(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("name")
      .add("position")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Sequence.class)
      .build();
  }

  @Override
  public Double getPosition() {
    return position;
  }

  /**
   Get Sequence id

   @return sequence id
   */
  public UUID getSequenceId() {
    return sequenceId;
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
  public SequenceChord setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public SequenceChord setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public SequenceChord setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  /**
   Set sequence id by providing the parent sequence

   @param sequence from which to set sequence id
   @return this SequenceChord (for chaining methods)
   */
  public SequenceChord setSequence(Sequence sequence) {
    return setSequenceId(sequence.getId());
  }

  /**
   Set sequence id

   @param sequenceId to set
   @return this SequenceChord (for chaining methods)
   */
  public SequenceChord setSequenceId(UUID sequenceId) {
    this.sequenceId = sequenceId;
    return this;
  }

  @Override
  public SequenceChord setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

  @Override
  public SequenceChord validate() throws CoreException {
    super.validate();
    Chord.validate(this);
    require(sequenceId, "Sequence ID");
    return this;
  }
}
