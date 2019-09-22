//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
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
public class Track extends ProgramSubEntity {
  private UUID voiceId;
  private String name;

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
      .add("name")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Voice.class)
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceHasMany())
      .add(Event.class)
      .build();
  }

  /**
   Get Voice ID

   @return voice id
   */
  public UUID getVoiceId() {
    return voiceId;
  }

  /**
   Set Name

   @param name to set
   @return this Track (for chaining setters)
   */
  public Track setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set voice UUID by providing a Voice entity

   @param voice id to set
   @return this Track (for chaining setters)
   */
  public Track setVoice(Voice voice) {
    setVoiceId(voice.getId());
    return this;
  }

  /**
   Set Voice ID

   @param voiceId to set
   @return this Track (for chaining setters)
   */
  public Track setVoiceId(UUID voiceId) {
    this.voiceId = voiceId;
    return this;
  }

  @Override
  public Track setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public Track validate() throws CoreException {
    super.validate();
    require(voiceId, "Voice ID");
    require(name, "Name");

    return this;
  }

  /**
   @return String representation of Track
   */
  public String toString() {
    return name;
  }
}
