// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentChoice extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("type")
    .add("transpose")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Segment.class)
    .add(Program.class)
    .add(ProgramSequenceBinding.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(SegmentChoiceArrangement.class)
    .build();
  private UUID segmentId;
  private UUID programId;
  private UUID programSequenceBindingId;
  private ProgramType type;
  private Integer transpose;

  /**
   of Choice

   @return new Choice
   */
  public static SegmentChoice create() {
    return new SegmentChoice().setId(UUID.randomUUID());
  }

  /**
   of Choice

   @param segment of SegmentChoice
   @return new Choice
   */
  public static SegmentChoice create(Segment segment) {
    return create()
      .setSegmentId(segment.getId());
  }

  /**
   of Choice

   @param segment                of SegmentChoice
   @param type                   of SegmentChoice
   @param programSequenceBinding of SegmentChoice, or null if none should be specified
   @param transpose              of SegmentChoice
   @return new Choice
   */
  public static SegmentChoice create(Segment segment, ProgramType type, ProgramSequenceBinding programSequenceBinding, Integer transpose) {
    return create(segment)
      .setTypeEnum(type)
      .setProgramId(programSequenceBinding.getProgramId())
      .setTranspose(transpose)
      .setProgramSequenceBindingId(programSequenceBinding.getId());
  }

  /**
   of Choice

   @param segment   of SegmentChoice
   @param type      of SegmentChoice
   @param program   of SegmentChoice
   @param transpose of SegmentChoice
   @return new Choice
   */
  public static SegmentChoice create(Segment segment, ProgramType type, Program program, Integer transpose) {
    return create(segment)
      .setSegmentId(segment.getId())
      .setProgramId(program.getId())
      .setTypeEnum(type)
      .setTranspose(transpose);
  }

  /**
   Find first segment choice of a given type in a collection of segment choices

   @param segmentChoices to filter from
   @param type           to find one of
   @return segment choice of given type
   */
  public static SegmentChoice findFirstOfType(Collection<SegmentChoice> segmentChoices, ProgramType type) throws CoreException {
    Optional<SegmentChoice> found = segmentChoices.stream().filter(c -> c.getType().equals(type)).findFirst();
    if (found.isEmpty()) throw new CoreException(String.format("No %s-type choice found", type));
    return found.get();
  }

  @Override
  public UUID getParentId() {
    return segmentId;
  }

  /**
   Get id of Segment to which this entity belongs

   @return segment id
   */
  public UUID getSegmentId() {
    return segmentId;
  }

  /**
   Set id of Segment to which this entity belongs

   @param segmentId to which this entity belongs
   @return this Segment Entity (for chaining setters)
   */
  public SegmentChoice setSegmentId(UUID segmentId) {
    this.segmentId = segmentId;
    return this;
  }

  /**
   Get transpose

   @return transpose
   */
  public Integer getTranspose() {
    return transpose;
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

  /**
   Get program id

   @return program id
   */
  public UUID getProgramId() {
    return programId;
  }

  /**
   Sequence Binding UUID

   @return sequence binding id
   */
  public UUID getProgramSequenceBindingId() {
    return programSequenceBindingId;
  }

  /**
   Get type

   @return type
   */
  public ProgramType getType() {
    return type;
  }

  /**
   Set program ID

   @param programId to set
   @return this Choice (for chaining setters)
   */
  public SegmentChoice setProgramId(UUID programId) {
    this.programId = programId;
    return this;
  }

  /**
   Set Sequence Binding UUID by providing a sequence binding

   @param sequenceBinding to set id of
   @return this Choice (for chaining setters)
   */
  public SegmentChoice setSequenceBinding(ProgramSequenceBinding sequenceBinding) {
    setProgramSequenceBindingId(sequenceBinding.getId());
    return this;
  }

  /**
   Set Sequence Binding UUID

   @param programSequenceBindingId to set
   @return this Choice (for chaining setters)
   */
  public SegmentChoice setProgramSequenceBindingId(UUID programSequenceBindingId) {
    this.programSequenceBindingId = programSequenceBindingId;
    return this;
  }

  /**
   Set type

   @param type to set
   @return this Choice (for chaining setters)
   */
  public SegmentChoice setType(String type) {
    this.type = ProgramType.valueOf(type);
    return this;
  }

  /**
   Set type

   @param type to set
   @return this Choice (for chaining setters)
   */
  public SegmentChoice setTypeEnum(ProgramType type) {
    this.type = type;
    return this;
  }

  /**
   Set transpose +/- semitone

   @param transpose to set
   @return this Choice (for chaining setters)
   */
  public SegmentChoice setTranspose(Integer transpose) {
    this.transpose = transpose;
    return this;
  }

  @Override
  public SegmentChoice setId(UUID id) {
    this.id = id;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    super.validate();

    require(segmentId, "Segment ID");
    require(programId, "Program ID");

    require(type, "Type");

    if (isEmpty(transpose)) transpose = 0;
  }
}
