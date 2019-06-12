//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.entity.impl.SuperEntityImpl;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.transport.GsonProvider;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentImpl extends SuperEntityImpl implements Segment {
  private final GsonProvider gsonProvider;
  private final Map<String, Object> report = Maps.newHashMap();
  private final Map<UUID, Arrangement> arrangementMap = Maps.newHashMap();
  private final Map<UUID, Choice> choiceMap = Maps.newHashMap();
  private final Map<UUID, Pick> pickMap = Maps.newHashMap();
  private final Map<UUID, SegmentChord> chordMap = Maps.newHashMap();
  private final Map<UUID, SegmentMeme> memeMap = Maps.newHashMap();
  private final Map<UUID, SegmentMessage> messageMap = Maps.newHashMap();
  private BigInteger chainId;
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
  private FabricatorType type;
  private Exception stateException;

  /**
   Constructor with Segment id
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
   Constructor with no id
   */
  @AssistedInject
  public SegmentImpl(
    GsonProvider gsonProvider
  ) {
    this.gsonProvider = gsonProvider;
  }

  @Override
  public Arrangement add(Arrangement arrangement) {
    try {
      arrangement.setSegmentId(getId());
      ensureRelations(arrangement);
      return SubEntity.add(arrangementMap, arrangement);
    } catch (CoreException e) {
      add(e);
      return arrangement;
    }
  }

  @Override
  public Choice add(Choice choice) {
    try {
      choice.setSegmentId(getId());
      return SubEntity.add(choiceMap, choice);
    } catch (CoreException e) {
      add(e);
      return choice;
    }
  }

  @Override
  public Pick add(Pick pick) {
    try {
      pick.setSegmentId(getId());
      ensureRelations(pick);
      return SubEntity.add(pickMap, pick);
    } catch (CoreException e) {
      add(e);
      return pick;
    }
  }

  @Override
  public SegmentChord add(SegmentChord chord) {
    try {
      chord.setSegmentId(getId());
      return SubEntity.add(chordMap, chord);
    } catch (CoreException e) {
      add(e);
      return chord;
    }
  }

  @Override
  public SegmentMeme add(SegmentMeme meme) {
    try {
      meme.setSegmentId(getId());
      return SubEntity.add(memeMap, meme);
    } catch (CoreException e) {
      add(e);
      return meme;
    }
  }

  @Override
  public SegmentMessage add(SegmentMessage message) {
    try {
      message.setSegmentId(getId());
      return SubEntity.add(messageMap, message);
    } catch (CoreException e) {
      add(e);
      return message;
    }
  }

  @Override
  public Segment consume(Payload payload) throws CoreException {
    super.consume(payload);
    syncSubEntities(payload, choiceMap, Choice.class);
    syncSubEntities(payload, chordMap, SegmentChord.class);
    syncSubEntities(payload, memeMap, SegmentMeme.class);
    syncSubEntities(payload, messageMap, SegmentMessage.class);
    syncSubEntities(payload, arrangementMap, Arrangement.class);
    syncSubEntities(payload, pickMap, Pick.class);
    return this;
  }

  @Override
  public Collection<SubEntity> getAllSubEntities() {
    Collection<SubEntity> out = Lists.newArrayList();
    out.addAll(getMemes());
    out.addAll(getMessages());
    out.addAll(getChoices());
    out.addAll(getArrangements());
    out.addAll(getChords());

    // FUTURE: picks are available via API on request, for example to generate detailed visualizations
    // out.addAll(getPicks());

    return out;
  }

  @Override
  public Collection<Arrangement> getArrangements() {
    return arrangementMap.values();
  }

  @Override
  public Collection<Arrangement> getArrangementsForChoice(Choice choice) {
    return getArrangements().stream().filter(arrangement -> choice.getId().equals(arrangement.getChoiceId())).collect(Collectors.toList());
  }

  @Override
  public Instant getBeginAt() {
    return beginAt;
  }

  @Override
  public BigInteger getChainId() {
    return chainId;
  }

  @Override
  public Choice getChoice(UUID id) throws CoreException {
    if (!choiceMap.containsKey(id))
      throw new CoreException(String.format("Found no Choice id=%s", id));
    return choiceMap.get(id);
  }

  @Override
  public Choice getChoiceOfType(ProgramType type) throws CoreException {
    Collection<Choice> out = getChoicesOfType(type);
    if (out.isEmpty())
      throw new CoreException(String.format("Found no Choice type=%s", type));
    return out.iterator().next();
  }

  @Override
  public Collection<Choice> getChoices() {
    return choiceMap.values();
  }

  @Override
  public Collection<Choice> getChoicesOfType(ProgramType type) {
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
  public Instant getEndAt() {
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
  public Long getOffset() {
    return offset;
  }

  @Override
  public BigInteger getParentId() {
    return chainId;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return ImmutableList.<String>builder()
      .addAll(super.getResourceAttributeNames())
      .add("state")
      .add("beginAt")
      .add("endAt")
      .add("key")
      .add("total")
      .add("offset")
      .add("density")
      .add("tempo")
      .add("waveformKey")
      .add("type")
      .build();
  }

  @Override
  public Collection<Pick> getPicks() {
    return pickMap.values();
  }

  @Override
  public Long getPreviousOffset() throws CoreException {
    if (isInitial())
      throw new CoreException("Cannot get previous id of initial Segment!");
    return offset - 1;
  }

  @Override
  public Map<String, Object> getReport() {
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
    return 0L == offset;
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
  public void setArrangements(Collection<Arrangement> arrangements) {
    arrangementMap.clear();
    for (Arrangement arrangement : arrangements) {
      add(arrangement);
    }
  }

  @Override
  public Segment setBeginAt(String beginAt) {
    try {
      this.beginAt = Instant.parse(beginAt);
    } catch (Exception e) {
      beginAtException = e;
    }
    return this;
  }

  @Override
  public Segment setBeginAtInstant(Instant beginAt) {
    this.beginAt = beginAt;
    return this;
  }

  @Override
  public Segment setChainId(BigInteger chainId) {
    this.chainId = chainId;
    return this;
  }

  @Override
  public void setChoices(Collection<Choice> choices) {
    choiceMap.clear();
    for (Choice choice : choices) {
      add(choice);
    }
  }

  @Override
  public void setChords(Collection<SegmentChord> chords) {
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
  public Segment setCreatedAt(String createdAt) {
    super.setCreatedAt(createdAt);
    return this;
  }

  @Override
  public Segment setCreatedAtInstant(Instant createdAt) {
    super.setCreatedAtInstant(createdAt);
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
      this.endAt = Instant.parse(endAt);
    } catch (Exception e) {
      endAtException = e;
    }
    return this;
  }

  @Override
  public Segment setEndAtInstant(Instant endAt) {
    this.endAt = endAt;
    return this;
  }

  @Override
  public Segment setKey(String key) {
    this.key = key;
    return this;
  }

  @Override
  public void setMemes(Collection<SegmentMeme> memes) {
    memeMap.clear();
    for (SegmentMeme meme : memes) {
      add(meme);
    }
  }

  @Override
  public void setMessages(Collection<SegmentMessage> messages) {
    messageMap.clear();
    for (SegmentMessage message : messages) {
      add(message);
    }
  }

  @Override
  public Segment setOffset(Long offset) {
    if (Objects.nonNull(offset)) {
      this.offset = offset;
    } else {
      this.offset = 0L;
    }
    return this;
  }

  @Override
  public void setPicks(Collection<Pick> picks) {
    pickMap.clear();
    for (Pick pick : picks) {
      add(pick);
    }
  }

  @Override
  public void setReport(Map<String, Object> input) {
    input.forEach(report::put);
  }

  @Override
  public Segment setState(String value) {
    try {
      state = SegmentState.validate(value);
    } catch (CoreException e) {
      stateException = e;
    }
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
  public Segment setUpdatedAt(String updatedAt) {
    super.setUpdatedAt(updatedAt);
    return this;
  }

  @Override
  public Segment setUpdatedAtInstant(Instant updatedAt) {
    super.setUpdatedAtInstant(updatedAt);
    return this;
  }

  @Override
  public Segment setWaveformKey(String waveformKey) {
    this.waveformKey = waveformKey;
    return this;
  }

  @Override
  public Segment validate() throws CoreException {
    require(chainId, "Chain ID");
    require(offset, "Offset");

    requireNo(stateException, "State");
    require(state, "State");

    requireNo(beginAtException, "Begin-at");
    require(beginAt, "Begin-at");

    requireNo(endAtException, "End-at");

    return validateContent();
  }

  @Override
  public Segment validateContent() throws CoreException {
    SubEntity.validate(this.getAllSubEntities());
    for (Arrangement arrangement : getArrangements()) ensureRelations(arrangement);
    for (Pick pick : getPicks()) ensureRelations(pick);
    return this;
  }

  /**
   Ensure that an Arrangement relates to an existing Choice stored in the Segment

   @param arrangement to ensure existing relations of
   @throws CoreException if no such Choice exists
   */
  private void ensureRelations(Arrangement arrangement) throws CoreException {
    if (Objects.isNull(arrangement.getChoiceId())) {
      throw new CoreException(String.format("Arrangement id=%s has null choiceId", arrangement.getId()));
    }
    if (!choiceMap.containsKey(arrangement.getChoiceId())) {
      throw new CoreException(String.format("Arrangement id=%s has nonexistent choiceId=%s", arrangement.getId(), arrangement.getChoiceId()));
    }
  }

  /**
   Ensure that an Pick relates to an existing Arrangement stored in the Segment

   @param pick to ensure existing relations of
   @throws CoreException if no such Arrangement exists
   */
  private void ensureRelations(Pick pick) throws CoreException {
    if (Objects.isNull(pick.getArrangementId())) {
      throw new CoreException(String.format("Pick id=%s has null arrangementId", pick.getId()));
    }
    if (!arrangementMap.containsKey(pick.getArrangementId())) {
      throw new CoreException(String.format("Pick id=%s has nonexistent arrangementId=%s", pick.getId(), pick.getArrangementId()));
    }
  }

}
