// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.UUID;

/**
 [#175177204] Artist editing main program sequences sets voicing for all Chords
 */
public class ProgramSequenceChordVoicing extends Entity {
  private UUID programId;
  private UUID programSequenceChordId;
  private InstrumentType type;
  private String notes;

  /**
   Create a new SequenceChordVoicing

   @param type          of voice
   @param sequenceChord to of voicing in
   @param notes         of voicing
   @return new sequence chord voicing
   */
  public static ProgramSequenceChordVoicing create(InstrumentType type, ProgramSequenceChord sequenceChord, String notes) {
    return create()
      .setProgramId(sequenceChord.getProgramId())
      .setType(type)
      .setSequenceChord(sequenceChord)
      .setNotes(notes);
  }

  /**
   Create a new SequenceChordVoicing

   @return new sequence chord voicing
   */
  public static ProgramSequenceChordVoicing create() {
    return (ProgramSequenceChordVoicing) new ProgramSequenceChordVoicing().setId(UUID.randomUUID());
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
  public ProgramSequenceChordVoicing setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get sequence chord UUID

   @return sequence chord UUID
   */
  public UUID getProgramSequenceChordId() {
    return programSequenceChordId;
  }

  /**
   Set Notes

   @param notes to set
   @return this Sequence Chord Voicing (for chaining methods)
   */
  public ProgramSequenceChordVoicing setNotes(String notes) {
    this.notes = notes;
    return this;
  }

  /**
   get type

   @return type of voicing
   */
  public InstrumentType getType() {
    return type;
  }

  /**
   set type

   @param type of voicing
   @return this Sequence Chord Voicing (for chaining methods)
   */
  public ProgramSequenceChordVoicing setType(InstrumentType type) {
    this.type = type;
    return this;
  }

  /**
   @return notes
   */
  public String getNotes() {
    return notes;
  }

  /**
   Set Sequence Chord UUID by providing the sequence chord itself

   @param sequenceChord to set UUID of
   @return this Sequence Chord Voicing (for chaining methods)
   */
  public ProgramSequenceChordVoicing setSequenceChord(ProgramSequenceChord sequenceChord) {
    return setProgramSequenceChordId(sequenceChord.getId());
  }

  /**
   Set Sequence Chord UUID

   @param programSequenceChordId to set
   @return this Sequence Chord Voicing (for chaining methods)
   */
  public ProgramSequenceChordVoicing setProgramSequenceChordId(UUID programSequenceChordId) {
    this.programSequenceChordId = programSequenceChordId;
    return this;
  }

  @Override
  public String toString() {
    return String.format("SequenceChordVoicing[%s]-%s", programSequenceChordId, notes);
  }

  @Override
  public void validate() throws ValueException {
    Value.require(programId, "Program ID");
    Value.require(programSequenceChordId, "Sequence Chord ID");
    Value.require(type, "Voice type");
    Value.require(notes, "Notes are required");
  }

}
