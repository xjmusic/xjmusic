// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.transport.CSV;

import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LinkChoice {
  public static final String KEY_IDEA_ID = "ideaId";
  public static final String KEY_PHASE_OFFSET = "phaseOffset";
  public static final String KEY_TRANSPOSE = "transpose";
  public static final String KEY_TYPE = "type";
  public static final String KEY_AVAILABLE_PHASE_OFFSETS = "availablePhaseOffsets";

  private ULong ideaId;
  private ULong phaseOffset;
  private int transpose;
  private String type;
  private List<ULong> availablePhaseOffsets;

  /**
   Construct a new LinkChoice utility object from a custom record

   @param record to instantiate LinkChoice from
   */
  private LinkChoice(Record record) {
    ideaId = (ULong) record.get(KEY_IDEA_ID);
    phaseOffset = (ULong) record.get(KEY_PHASE_OFFSET);
    transpose = (int) record.get(KEY_TRANSPOSE);
    type = (String) record.get(KEY_TYPE);
    availablePhaseOffsets = Lists.newArrayList();
    CSV.split((String) record.get(KEY_AVAILABLE_PHASE_OFFSETS))
      .forEach(phaseOffset ->
        availablePhaseOffsets.add(ULong.valueOf(phaseOffset)));
    Collections.sort(availablePhaseOffsets);
  }

  @Nullable
  public static LinkChoice from(@Nullable Record record) {
    if (Objects.isNull(record))
      return null;

    return new LinkChoice(record);
  }

  /**
   Construct a new LinkChoice manually

   @param ideaId                of choice
   @param phaseOffset           of chosen idea
   @param transpose             +/- semitones of chosen idea
   @param type                  of choice
   @param availablePhaseOffsets all eitherOr phase offsets for this idea
   */
  LinkChoice(ULong ideaId, ULong phaseOffset, int transpose, String type, List<ULong> availablePhaseOffsets) {
    this.ideaId = ideaId;
    this.phaseOffset = phaseOffset;
    this.transpose = transpose;
    this.type = type;
    this.availablePhaseOffsets = availablePhaseOffsets;
  }

  /**
   Whether the current Link Choice has one or more phases
   with a higher phase offset than the current one

   @return true if it has one more phase
   */
  public boolean hasOneMorePhase() {
    for (ULong availableOffset : availablePhaseOffsets)
      if (availableOffset.compareTo(phaseOffset) == 1)
        return true;
    return false;
  }

  /**
   Whether the current Link Choice has two or more phases
   with a higher phase offset than the current two

   @return true if it has two more phase
   */
  public boolean hasTwoMorePhases() {
    int num = 0;
    for (ULong availableOffset : availablePhaseOffsets)
      if (availableOffset.compareTo(phaseOffset) == 1) {
        num++;
        if (num >= 2)
          return true;
      }
    return false;
  }

  /**
   Returns the phase offset immediately after the current one,
   or loop back to zero is past the end of the available phases

   @return next phase offset
   */
  @Nullable
  public ULong nextPhaseOffset() {
    ULong offset = null;
    for (ULong availableOffset : availablePhaseOffsets)
      if (availableOffset.compareTo(phaseOffset) == 1)
        if (Objects.isNull(offset) ||
          availableOffset.compareTo(offset) == -1)
          offset = availableOffset;
    return Objects.nonNull(offset) ? offset : ULong.valueOf(0);
  }

  /**
   Get eitherOr phase offsets for the chosen idea

   @return eitherOr phase offsets
   */
  public List<ULong> getAvailablePhaseOffsets() {
    return availablePhaseOffsets;
  }

  /**
   Get the chosen idea id

   @return chosen idea id
   */
  public ULong getIdeaId() {
    return ideaId;
  }

  /**
   get the phase offset of the chosen idea

   @return chosen idea phase offset
   */
  public ULong getPhaseOffset() {
    return phaseOffset;
  }

  /**
   get the transpose +/- semitones of the chosen idea

   @return chosen idea transpose +/- semitones
   */
  public int getTranspose() {
    return transpose;
  }

  /**
   get the type of the choice

   @return type of choice
   */
  public String getType() {
    return type;
  }

  /**
   Return the link choice as a map, for reporting

   @return report map
   */
  public Map<String, Object> asMap() {
    Map<String, Object> out = Maps.newHashMap();
    out.put(KEY_IDEA_ID, ideaId);
    out.put(KEY_PHASE_OFFSET, phaseOffset);
    out.put(KEY_TRANSPOSE, transpose);
    out.put(KEY_TYPE, type);
    out.put(KEY_AVAILABLE_PHASE_OFFSETS, availablePhaseOffsets);
    return out;
  }

}
