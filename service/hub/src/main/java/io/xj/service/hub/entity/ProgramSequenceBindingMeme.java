// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.entity.MemeEntity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.UUID;

/**
 [#166690830] Program model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class ProgramSequenceBindingMeme extends MemeEntity {


  private UUID programId;
  private UUID programSequenceBindingId;

  /**
   Create a new SequenceBindingMeme

   @param sequenceBinding to of meme in
   @param name            of meme
   @return new sequence binding meme
   */
  public static ProgramSequenceBindingMeme create(ProgramSequenceBinding sequenceBinding, String name) {
    return create()
      .setProgramId(sequenceBinding.getProgramId())
      .setSequenceBinding(sequenceBinding)
      .setName(name);
  }

  /**
   Create a new SequenceBindingMeme

   @return new sequence binding meme
   */
  public static ProgramSequenceBindingMeme create() {
    return (ProgramSequenceBindingMeme) new ProgramSequenceBindingMeme().setId(UUID.randomUUID());
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
  public ProgramSequenceBindingMeme setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Get sequence binding UUID

   @return sequence binding UUID
   */
  public UUID getProgramSequenceBindingId() {
    return programSequenceBindingId;
  }

  @Override
  public ProgramSequenceBindingMeme setName(String name) {
    super.setName(name);
    return this;
  }

  /**
   Set Sequence Binding UUID by providing the sequence binding itself

   @param sequenceBinding to set UUID of
   @return this Sequence Binding MemeEntity (for chaining methods)
   */
  public ProgramSequenceBindingMeme setSequenceBinding(ProgramSequenceBinding sequenceBinding) {
    return setProgramSequenceBindingId(sequenceBinding.getId());
  }

  /**
   Set Sequence Binding UUID

   @param programSequenceBindingId to set
   @return this Sequence Binding MemeEntity (for chaining methods)
   */
  public ProgramSequenceBindingMeme setProgramSequenceBindingId(UUID programSequenceBindingId) {
    this.programSequenceBindingId = programSequenceBindingId;
    return this;
  }

  @Override
  public String toString() {
    return String.format("SequenceBinding[%s]-%s", getProgramSequenceBindingId(), getName());
  }

  @Override
  public void validate() throws ValueException {
    Value.require(programId, "Program ID");
    Value.require(programSequenceBindingId, "Sequence Binding ID");
    MemeEntity.validate(this);
  }

}
