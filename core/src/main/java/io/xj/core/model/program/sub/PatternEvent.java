// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program.sub;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Event;
import io.xj.core.model.program.impl.ProgramSubEntity;
import io.xj.core.util.Text;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166690830] Pattern model handles all of its own entities
 */
public class PatternEvent extends ProgramSubEntity implements Event {
  protected Double duration;
  protected String inflection;
  protected String note;
  protected Double position;
  protected Double velocity;
  private UUID patternId;

  @Override
  public Double getDuration() {
    return duration;
  }

  @Override
  public String getInflection() {
    return inflection;
  }

  @Override
  public String getNote() {
    return note;
  }

  /**
   Get Pattern UUID

   @return Pattern UUID
   */
  public UUID getPatternId() {
    return patternId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("duration")
      .add("inflection")
      .add("note")
      .add("position")
      .add("velocity")
      .build();
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Pattern.class)
      .build();
  }

  @Override
  public Double getPosition() {
    return position;
  }

  @Override
  public Double getVelocity() {
    return velocity;
  }

  @Override
  public PatternEvent setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  @Override
  public PatternEvent setInflection(String inflection) {
    this.inflection = Text.toUpperSlug(inflection);
    return this;
  }

  @Override
  public PatternEvent setNote(String note) {
    this.note = Text.toNote(note);
    return this;
  }

  /**
   Set Pattern UUID by providing the parent Pattern

   @param pattern to set UUID of
   @return this PatternEvent (for chaining methods)
   */
  public PatternEvent setPattern(Pattern pattern) {
    setPatternId(pattern.getId());
    return this;
  }

  /**
   Set Pattern UUID

   @param patternId to set
   @return this PatternEvent (for chaining methods)
   */
  public PatternEvent setPatternId(UUID patternId) {
    this.patternId = patternId;
    return this;
  }

  @Override
  public PatternEvent setPosition(Double position) {
    this.position = Value.limitFloatingPointPlaces(position);
    return this;
  }

  @Override
  public PatternEvent setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public PatternEvent setVelocity(Double velocity) {
    this.velocity = velocity;
    return this;
  }

  @Override
  public PatternEvent setProgramId(BigInteger programId) {
    super.setProgramId(programId);
    return this;
  }

  @Override
  public PatternEvent validate() throws CoreException {
    super.validate();
    Event.validate(this);
    require(patternId, "Pattern ID");
    return this;
  }
}
