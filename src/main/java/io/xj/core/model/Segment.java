// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Segment extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("state")
    .add("beginAt")
    .add("endAt")
    .add("key")
    .add("total")
    .add("offset")
    .add("density")
    .add("tempo")
    .add("waveformKey")
    .add("waveformPreroll")
    .add("type")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Chain.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(SegmentChoiceArrangement.class)
    .add(SegmentChoice.class)
    .add(SegmentChoiceArrangementPick.class)
    .add(SegmentChord.class)
    .add(SegmentMeme.class)
    .add(SegmentMessage.class)
    .build();
  private UUID chainId;
  private SegmentState state;
  private Instant beginAt;
  private Exception beginAtException;
  private Instant endAt; // optional
  private Exception endAtException;
  private String key;
  private Integer total;
  private Long offset;
  private Double density;
  private Double tempo;
  private String waveformKey;
  private Double waveformPreroll;
  private FabricatorType type;
  private Exception stateException;

  /**
   of Segment

   @return new segment
   */
  public static Segment create() {
    return new Segment().setId(UUID.randomUUID()).setWaveformPreroll(0.0);
  }

  /**
   Create a new Segment

   @param chain       of Segment
   @param offset      of Segment
   @param state       of Segment
   @param beginAt     of Segment
   @param endAt       of Segment
   @param key         of Segment
   @param total       of Segment
   @param density     of Segment
   @param tempo       of Segment
   @param waveformKey of Segment
   @return new Segment
   */
  public static Segment create(Chain chain, long offset, SegmentState state, Instant beginAt, Instant endAt, String key, int total, double density, double tempo, String waveformKey) {
    return create()
      .setChainId(chain.getId())
      .setOffset(offset)
      .setState(state.toString())
      .setBeginAtInstant(beginAt)
      .setEndAtInstant(endAt)
      .setTotal(total)
      .setKey(key)
      .setDensity(density)
      .setTempo(tempo)
      .setWaveformKey(waveformKey);
  }

  /**
   Create a new planned-state segment with no endAt or properties

   @param chain   to create segment in
   @param offset  of segment
   @param beginAt of segment
   @return new segment
   */
  public static Segment create(Chain chain, long offset, Instant beginAt) {
    return create()
      .setStateEnum(SegmentState.Planned)
      .setOffset(offset)
      .setChainId(chain.getId())
      .setBeginAtInstant(beginAt);
  }

  /**
   get BeginAt

   @return BeginAt
   */
  public Instant getBeginAt() {
    return beginAt;
  }

  /**
   get ChainId

   @return ChainId
   */
  public UUID getChainId() {
    return chainId;
  }

  /**
   get Density

   @return Density
   */
  public Double getDensity() {
    return density;
  }

  /**
   get EndAt

   @return EndAt
   */
  public Instant getEndAt() {
    return endAt;
  }

  /**
   get Key

   @return Key
   */
  public String getKey() {
    return key;
  }

  /**
   get Offset

   @return Offset
   */
  public Long getOffset() {
    return offset;
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
   get offset of previous segment

   @return previous segment offset
   */
  public Long getPreviousOffset() throws CoreException {
    if (isInitial())
      throw new CoreException("Cannot get previous id create initial Segment!");
    return offset - 1;
  }

  /**
   get State

   @return State
   */
  public SegmentState getState() {
    return state;
  }

  /**
   get Total

   @return Total
   */
  public Integer getTotal() {
    return total;
  }

  /**
   get Tempo

   @return Tempo
   */
  public Double getTempo() {
    return tempo;
  }

  /**
   get Type

   @return Type
   */
  public FabricatorType getType() {
    return type;
  }

  /**
   get WaveformKey

   @return WaveformKey
   */
  public String getWaveformKey() {
    return waveformKey;
  }

  /**
   get WaveformPreroll

   @return WaveformPreroll
   */
  public Double getWaveformPreroll() {
    return waveformPreroll;
  }

  /**
   Is initial segment? (offset 0)

   @return true if offset 0
   */
  public boolean isInitial() {
    return 0L == offset;
  }

  /**
   Set the beginAt

   @param beginAt to set
   @return this Segment (for chaining setters)
   */
  public Segment setBeginAt(String beginAt) {
    try {
      this.beginAt = Instant.parse(beginAt);
    } catch (Exception e) {
      beginAtException = e;
    }
    return this;
  }

  /**
   Set the beginAt

   @param beginAt to set
   @return this Segment (for chaining setters)
   */
  public Segment setBeginAtInstant(Instant beginAt) {
    this.beginAt = beginAt;
    return this;
  }

  /**
   Set the chainId

   @param chainId to set
   @return this Segment (for chaining setters)
   */
  public Segment setChainId(UUID chainId) {
    this.chainId = chainId;
    return this;
  }

  @Override
  public Segment setCreatedAt(String createdAt) {
    super.setCreatedAt(createdAt);
    return this;
  }

  @Override
  public Segment setCreatedAtInstant(Instant createdAt) {
    super.setCreatedAtInstant(createdAt);
    return this;
  }

  /**
   Set the id

   @param id to set
   @return this Segment (for chaining setters)
   */
  public Segment setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   Set the density

   @param density to set
   @return this Segment (for chaining setters)
   */
  public Segment setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set the endAt

   @param endAt to set
   @return this Segment (for chaining setters)
   */
  public Segment setEndAt(String endAt) {
    try {
      this.endAt = Instant.parse(endAt);
    } catch (Exception e) {
      endAtException = e;
    }
    return this;
  }

  /**
   Set the endAt

   @param endAt to set
   @return this Segment (for chaining setters)
   */
  public Segment setEndAtInstant(Instant endAt) {
    this.endAt = endAt;
    return this;
  }

  /**
   Set the key

   @param key to set
   @return this Segment (for chaining setters)
   */
  public Segment setKey(String key) {
    this.key = key;
    return this;
  }

  /**
   Set the offset

   @param offset to set
   @return this Segment (for chaining setters)
   */
  public Segment setOffset(Long offset) {
    if (Objects.nonNull(offset)) {
      this.offset = offset;
    } else {
      this.offset = 0L;
    }
    return this;
  }


  /**
   Set the value

   @param value to set
   @return this Segment (for chaining setters)
   */
  public Segment setState(String value) {
    try {
      state = SegmentState.validate(value);
    } catch (CoreException e) {
      stateException = e;
    }
    return this;
  }

  /**
   Set the value

   @param value to set
   @return this Segment (for chaining setters)
   */
  public Segment setStateEnum(SegmentState value) {
    state = value;
    return this;
  }

  /**
   Set the tempo

   @param tempo to set
   @return this Segment (for chaining setters)
   */
  public Segment setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set the total

   @param total to set
   @return this Segment (for chaining setters)
   */
  public Segment setTotal(Integer total) {
    this.total = total;
    return this;
  }

  /**
   Set the type

   @param type to set
   @return this Segment (for chaining setters)
   */
  public Segment setType(String type) {
    this.type = FabricatorType.valueOf(type);
    return this;
  }

  /**
   Set the type

   @param type to set
   @return this Segment (for chaining setters)
   */
  public Segment setTypeEnum(FabricatorType type) {
    this.type = type;
    return this;
  }

  @Override
  public Segment setUpdatedAt(String updatedAt) {
    super.setUpdatedAt(updatedAt);
    return this;
  }

  @Override
  public Segment setUpdatedAtInstant(Instant updatedAt) {
    super.setUpdatedAtInstant(updatedAt);
    return this;
  }

  /**
   Set the waveformKey

   @param waveformKey to set
   @return this Segment (for chaining setters)
   */
  public Segment setWaveformKey(String waveformKey) {
    this.waveformKey = waveformKey;
    return this;
  }

  /**
   Set the waveformPreroll

   @param waveformPreroll to set
   @return this Segment (for chaining setters)
   */
  public Segment setWaveformPreroll(Double waveformPreroll) {
    this.waveformPreroll = waveformPreroll;
    return this;
  }

  @Override
  public String toString() {
    return String.format("Segment[%s]-offset@%d-in-Chain[%s]", getId().toString(), getOffset(), getChainId().toString());
  }

  @Override
  public void validate() throws CoreException {
    require(chainId, "Chain ID");
    require(offset, "Offset");

    if (Objects.isNull(waveformPreroll)) waveformPreroll = 0.0;

    requireNo(stateException, "State");
    require(state, "State");

    requireNo(beginAtException, "Begin-at");
    require(beginAt, "Begin-at");

    requireNo(endAtException, "End-at");
  }

}
