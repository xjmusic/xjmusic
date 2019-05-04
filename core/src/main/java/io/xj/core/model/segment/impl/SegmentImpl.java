//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.impl.EntityImpl;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentEntity;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.transport.GsonProvider;
import io.xj.core.util.TimestampUTC;
import io.xj.core.util.Value;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentImpl extends EntityImpl implements Segment {
  private final GsonProvider gsonProvider;
  private final Map<String, String> report = Maps.newConcurrentMap();
  private final Map<UUID, Arrangement> arrangementMap = Maps.newConcurrentMap();
  private final Map<UUID, Choice> choiceMap = Maps.newConcurrentMap();
  private final Map<UUID, Pick> pickMap = Maps.newConcurrentMap();
  private final Map<UUID, SegmentChord> chordMap = Maps.newConcurrentMap();
  private final Map<UUID, SegmentMeme> memeMap = Maps.newConcurrentMap();
  private final Map<UUID, SegmentMessage> messageMap = Maps.newConcurrentMap();
  private BigInteger chainId;
  private SegmentState state;
  private Timestamp beginAt;
  private String beginAtError;
  private Timestamp endAt; // optional
  private String endAtError;
  private String key;
  private Integer total;
  private BigInteger offset;
  private Double density;
  private Double tempo;
  private String waveformKey;
  private FabricatorType type;

  /**
   Constructor with guice injection, including Segment id
   */
  @AssistedInject
  public SegmentImpl(
    @Assisted("id") BigInteger id,
    GsonProvider gsonProvider
  ) {
    this.id = id;
    this.gsonProvider = gsonProvider;
  }

  /**
   Constructor with guice injection, no id
   */
  @AssistedInject
  public SegmentImpl(
    GsonProvider gsonProvider
  ) {
    this.gsonProvider = gsonProvider;
  }

  /**
   Add an entity to its internal map
   Assign the next unique Id to an entity,
   unless that entity already as an Id, in which case, ensure that the next unique one will be higher.
   Also ensure that id does not already exist in its map

   @param entity to assign next unique id
   */
  private static <N extends SegmentEntity> N add(Map<UUID, N> entities, N entity) throws CoreException {
    if (Objects.isNull(entity.getUuid())) {
      entity.setUuid(UUID.randomUUID());
    } else {
      if (entities.containsKey(entity.getUuid())) {
        throw new CoreException(String.format("%s uuid=%s already exists", entity.getClass().getSimpleName(), entity.getUuid()));
      }
    }
    entity.validate();
    entities.put(entity.getUuid(), entity);
    return entity;
  }

  @Override
  public Arrangement add(Arrangement arrangement) throws CoreException {
    arrangement.setSegmentId(getId());
    ensureRelations(arrangement);
    return add(arrangementMap, arrangement);
  }

  @Override
  public Choice add(Choice choice) throws CoreException {
    choice.setSegmentId(getId());
    return add(choiceMap, choice);
  }

  @Override
  public Pick add(Pick pick) throws CoreException {
    pick.setSegmentId(getId());
    ensureRelations(pick);
    return add(pickMap, pick);
  }

  @Override
  public SegmentChord add(SegmentChord chord) throws CoreException {
    chord.setSegmentId(getId());
    return add(chordMap, chord);
  }

  @Override
  public SegmentMeme add(SegmentMeme meme) throws CoreException {
    meme.setSegmentId(getId());
    return add(memeMap, meme);
  }

  @Override
  public SegmentMessage add(SegmentMessage message) throws CoreException {
    message.setSegmentId(getId());
    return add(messageMap, message);
  }

  @Override
  public Collection<SegmentEntity> getAllEntities() {
    Collection<SegmentEntity> out = Lists.newArrayList();
    out.addAll(getMemes());
    out.addAll(getMessages());
    out.addAll(getChoices());
    out.addAll(getArrangements());
    out.addAll(getChords());
    out.addAll(getPicks());
    return out;
  }

  @Override
  public Collection<Arrangement> getArrangements() {
    return arrangementMap.values();
  }

  @Override
  public Collection<Arrangement> getArrangementsForChoice(Choice choice) {
    return getArrangements().stream().filter(arrangement -> choice.getUuid().equals(arrangement.getChoiceUuid())).collect(Collectors.toList());
  }

  @Override
  public Timestamp getBeginAt() {
    return beginAt;
  }

  @Override
  public BigInteger getChainId() {
    return chainId;
  }

  @Override
  public Choice getChoiceOfType(SequenceType type) throws CoreException {
    Collection<Choice> out = getChoicesOfType(type);
    if (out.isEmpty()) {
      throw new CoreException(String.format("Found no Choice type=%s", type));
    }
    return out.iterator().next();
  }

  @Override
  public Collection<Choice> getChoices() {
    return choiceMap.values();
  }

  @Override
  public Collection<Choice> getChoicesOfType(SequenceType type) {
    return getChoices()
      .stream()
      .filter(choice -> type == choice.getType())
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChord> getChords() {
    return chordMap.values();
  }

  @Override
  public SegmentContent getContent() {
    return SegmentContent.of(this);
  }

  @Override
  public Double getDensity() {
    return density;
  }

  @Override
  public Timestamp getEndAt() {
    return endAt;
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public Collection<SegmentMeme> getMemes() {
    return memeMap.values();
  }

  @Override
  public Collection<SegmentMessage> getMessages() {
    return messageMap.values();
  }

  @Override
  public BigInteger getOffset() {
    return offset;
  }

  @Override
  public BigInteger getParentId() {
    return chainId;
  }

  @Override
  public Collection<Pick> getPicks() {
    return pickMap.values();
  }

  @Override
  public BigInteger getPreviousOffset() throws CoreException {
    if (Objects.equals(offset, BigInteger.valueOf(0L)))
      throw new CoreException("Cannot get previous id of initial Segment!");
    return Value.inc(offset, -1);
  }

  @Override
  public Map<String, String> getReport() {
    return report;
  }

  @Override
  public SegmentState getState() {
    return state;
  }

  @Override
  public Integer getTotal() {
    return total;
  }

  @Override
  public Double getTempo() {
    return tempo;
  }

  @Override
  public FabricatorType getType() {
    return type;
  }

  @Override
  public String getWaveformKey() {
    return waveformKey;
  }

  @Override
  public boolean isInitial() {
    return Objects.equals(getOffset(), BigInteger.valueOf(0));
  }

  @Override
  public void putReport(String key, String value) {
    report.put(key, value);
  }

  @Override
  public void revert() {
    memeMap.clear();
    choiceMap.clear();
    arrangementMap.clear();
    chordMap.clear();
    pickMap.clear();
  }

  @Override
  public void setArrangements(Collection<Arrangement> arrangements) throws CoreException {
    arrangementMap.clear();
    for (Arrangement arrangement : arrangements) {
      add(arrangement);
    }
  }

  @Override
  public Segment setBeginAt(String beginAt) {
    try {
      this.beginAt = TimestampUTC.valueOf(beginAt);
    } catch (Exception e) {
      beginAtError = e.getMessage();
    }
    return this;
  }

  @Override
  public Segment setBeginAtTimestamp(Timestamp beginAt) {
    this.beginAt = beginAt;
    return this;
  }

  @Override
  public Segment setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  @Override
  public void setChoices(Collection<Choice> choices) throws CoreException {
    choiceMap.clear();
    for (Choice choice : choices) {
      add(choice);
    }
  }

  @Override
  public void setChords(Collection<SegmentChord> chords) throws CoreException {
    chordMap.clear();
    for (SegmentChord chord : chords) {
      add(chord);
    }
  }

  @Override
  public Segment setContent(String json) throws CoreException {
    SegmentContent content = gsonProvider.gson().fromJson(json, SegmentContent.class);
    setReport(content.getReport());
    setMemes(content.getMemes());
    setMessages(content.getMessages());
    setChoices(content.getChoices());
    setArrangements(content.getArrangements());
    setChords(content.getChords());
    setPicks(content.getPicks());
    setTypeEnum(content.getType());
    return this;
  }

  @Override
  public Segment setDensity(Double density) {
    this.density = density;
    return this;
  }

  @Override
  public Segment setEndAt(String endAt) {
    try {
      this.endAt = TimestampUTC.valueOf(endAt);
    } catch (Exception e) {
      endAtError = e.getMessage();
    }
    return this;
  }

  @Override
  public Segment setEndAtTimestamp(Timestamp endAt) {
    this.endAt = endAt;
    return this;
  }

  @Override
  public Segment setKey(String key) {
    this.key = key;
    return this;
  }

  @Override
  public void setMemes(Collection<SegmentMeme> memes) throws CoreException {
    memeMap.clear();
    for (SegmentMeme meme : memes) {
      add(meme);
    }
  }

  @Override
  public void setMessages(Collection<SegmentMessage> messages) throws CoreException {
    messageMap.clear();
    for (SegmentMessage message : messages) {
      add(message);
    }
  }

  @Override
  public Segment setOffset(BigInteger offset) {
    if (Objects.nonNull(offset)) {
      this.offset = offset;
    } else {
      this.offset = BigInteger.valueOf(0L);
    }
    return this;
  }

  @Override
  public void setPicks(Collection<Pick> picks) throws CoreException {
    pickMap.clear();
    for (Pick pick : picks) {
      add(pick);
    }
  }

  @Override
  public void setReport(Map<String, String> input) {
    input.forEach(report::put);
  }

  @Override
  public Segment setState(String value) throws CoreException {
    state = SegmentState.validate(value);
    return this;
  }

  @Override
  public Segment setStateEnum(SegmentState value) {
    state = value;
    return this;
  }

  @Override
  public Segment setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  @Override
  public Segment setTotal(Integer total) {
    this.total = total;
    return this;
  }

  @Override
  public void setType(String type) {
    this.type = FabricatorType.valueOf(type);
  }

  @Override
  public void setTypeEnum(FabricatorType type) {
    this.type = type;
  }

  @Override
  public Segment setWaveformKey(String waveformKey) {
    this.waveformKey = waveformKey;
    return this;
  }

  @Override
  public void validate() throws CoreException {
    if (Objects.isNull(state)) {
      throw new CoreException("State is required.");
    }

    if (Objects.isNull(chainId)) {
      throw new CoreException("Chain ID is required.");
    }

    if (Objects.isNull(beginAt)) {
      throw new CoreException("Begin-at is required." + (Objects.nonNull(beginAtError) ? " " + beginAtError : ""));
    }

    if (Objects.nonNull(endAtError) && !endAtError.isEmpty()) {
      throw new CoreException("End-at must be isValid time." + endAtError);
    }

    if (Objects.isNull(offset)) {
      throw new CoreException("Offset is required.");
    }

    validateContent();
  }

  @Override
  public void validateContent() throws CoreException {
    Collection<UUID> ids = Lists.newArrayList();
    for (SegmentEntity entity : getAllEntities()) {
      if (Objects.isNull(entity.getUuid())) {
        throw new CoreException(String.format("Contains a %s with null id", entity.getClass().getSimpleName()));
      }
      if (ids.contains(entity.getUuid())) {
        throw new CoreException(String.format("Contains %s with duplicate uuid=%s", entity.getClass().getSimpleName(), entity.getUuid()));
      }
      try {
        entity.validate();
      } catch (CoreException e) {
        throw new CoreException(String.format("%s with uuid=%s is invalid", entity.getClass().getSimpleName(), entity.getUuid()), e);
      }
      ids.add(entity.getUuid());
    }
    // arrangements have existing choice
    for (Arrangement arrangement : getArrangements()) {
      ensureRelations(arrangement);
    }
    // picks have existing arrangement
    for (Pick pick : getPicks()) {
      ensureRelations(pick);
    }
  }


  /**
   Ensure that an Arrangement relates to an existing Choice stored in the Segment

   @param arrangement to ensure existing relations of
   @throws CoreException if no such Choice exists
   */
  private void ensureRelations(Arrangement arrangement) throws CoreException {
    if (Objects.isNull(arrangement.getChoiceUuid())) {
      throw new CoreException(String.format("Arrangement id=%s has null choiceId", arrangement.getUuid()));
    }
    if (!choiceMap.containsKey(arrangement.getChoiceUuid())) {
      throw new CoreException(String.format("Arrangement id=%s has nonexistent choiceId=%s", arrangement.getUuid(), arrangement.getChoiceUuid()));
    }
  }

  /**
   Ensure that an Pick relates to an existing Arrangement stored in the Segment

   @param pick to ensure existing relations of
   @throws CoreException if no such Arrangement exists
   */
  private void ensureRelations(Pick pick) throws CoreException {
    if (Objects.isNull(pick.getArrangementUuid())) {
      throw new CoreException(String.format("Pick id=%s has null arrangementId", pick.getUuid()));
    }
    if (!arrangementMap.containsKey(pick.getArrangementUuid())) {
      throw new CoreException(String.format("Pick id=%s has nonexistent arrangementId=%s", pick.getUuid(), pick.getArrangementUuid()));
    }
  }

}
