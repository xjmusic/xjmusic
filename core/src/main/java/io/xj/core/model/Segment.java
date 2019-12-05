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
  private FabricatorType type;
  private Exception stateException;

  /**
   of Segment

   @return new segment
   */
  public static Segment create() {
    return new Segment().setId(UUID.randomUUID());
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

  @Override
  public String toString() {
    return String.format("Segment[%s]-offset@%d-in-Chain[%s]", getId().toString(), getOffset(), getChainId().toString());
  }

  @Override
  public void validate() throws CoreException {
    require(chainId, "Chain ID");
    require(offset, "Offset");

    requireNo(stateException, "State");
    require(state, "State");

    requireNo(beginAtException, "Begin-at");
    require(beginAt, "Begin-at");

    requireNo(endAtException, "End-at");
  }



/*


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
  public Collection<SegmentMeme> getMemes() {
    return memeMap.values();
  }

  @Override
  public Collection<SegmentMessage> getMessages() {
    return messageMap.values();
  }



  @Override
  public Collection<Pick> getPicks() {
    return pickMap.values();
  }

  @Override
  public void setArrangements(Collection<Arrangement> arrangements) {
    arrangementMap.clear();
    for (Arrangement arrangement : arrangements) {
      add(arrangement);
    }
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
  public void setPicks(Collection<Pick> picks) {
    pickMap.clear();
    for (Pick pick : picks) {
      add(pick);
    }
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
   *
  private void ensureRelations(Arrangement arrangement) throws CoreException {
    if (Objects.isNull(arrangement.getSegmentChoiceId())) {
      throw new CoreException(String.format("Arrangement id=%s has null choiceId", arrangement.getId()));
    }
    if (!choiceMap.containsKey(arrangement.getSegmentChoiceId())) {
      throw new CoreException(String.format("Arrangement id=%s has nonexistent choiceId=%s", arrangement.getId(), arrangement.getSegmentChoiceId()));
    }
  }

  /**
   Ensure that an Pick relates to an existing Arrangement stored in the Segment

   @param pick to ensure existing relations of
   @throws CoreException if no such Arrangement exists
   *
  private void ensureRelations(Pick pick) throws CoreException {
    if (Objects.isNull(pick.getSegmentChoiceArrangementId())) {
      throw new CoreException(String.format("Pick id=%s has null arrangementId", pick.getId()));
    }
    if (!arrangementMap.containsKey(pick.getSegmentChoiceArrangementId())) {
      throw new CoreException(String.format("Pick id=%s has nonexistent arrangementId=%s", pick.getId(), pick.getSegmentChoiceArrangementId()));
    }
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


*/

  /*

  TODO see if Segment needs any of these legacy interface methods

  /**
   Format a comma-separated list of entity counts of a collection of entities

   @param entities for format a comma-separated list of the # occurrences of each class
   @return comma-separated list in text
   *
  static <N extends Entity> String histogramString(Collection<N> entities) {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.of();
    entities.forEach((N entity) -> entityHistogram.add(Text.getSimpleName(entity)));
    List<String> descriptors = Lists.newArrayList();
    entityHistogram.elementSet().forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

  /**
   Get a string representation of an entity, comprising a key-value map of its properties

   @param name       of entity
   @param properties to map
   @return string representation
   *
  static String toKeyValueString(String name, ImmutableMap<String, String> properties) {
    return String.format("%s{%s}", name, CSV.of(properties));
  }

  /**
   Add an Arrangement to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param arrangement to add
   @return Arrangement with newly added unique-to-segment id
   *
  Arrangement add(Arrangement arrangement);

  /**
   Add a Choice to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param choice to add
   @return Choice with newly added unique-to-segment id
   *
  Choice add(Choice choice);

  /**
   Add a Pick to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param pick to add
   @return Pick with newly added unique-to-segment id
   *
  Pick add(Pick pick);

  /**
   Add a SegmentChord to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param chord to add
   @return SegmentChord with newly added unique-to-segment id
   *
  SegmentChord add(SegmentChord chord);

  /**
   Add a SegmentMeme to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param meme to add
   @return SegmentMeme with newly added unique-to-segment id
   *
  SegmentMeme add(SegmentMeme meme);

  /**
   Add a SegmentMessage to Segment
   + If there are any exceptions, store them in the SuperEntity errors

   @param message to add
   @return SegmentMessage with newly added unique-to-segment id
   *
  SegmentMessage add(SegmentMessage message);

  /**
     Get all entities contained within this entity.
     Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

     @return collection of entities
     *
  Collection<SubEntity> getAllSubEntities();

  /**
   Get Arrangements

   @return Arrangements
   *
  Collection<Arrangement> getArrangements();

  /**
   Get begin-at time of segment

   @return begin-at time
   *
  Instant getBeginAt();

  /**
   Get Chain ID

   @return chain id
   *
  UUID getChainId();

  /**
   Get a Choice by UUID

   @param id of choice
   @return Choice
   @throws CoreException if no such choice exists
   *
  Choice getChoice(UUID id) throws CoreException;

  /**
   Get one Choice of Segment (of Content) of a given type

   @param type of choice to get
   @return choice of given type
   @throws CoreException if no such Choice exists for this Segment
   *
  Choice getChoiceOfType(ProgramType type) throws CoreException;

  /**
   Get Choices

   @return Choices
   *
  Collection<Choice> getChoices();

  /**
   Get all Choices of Segment (of Content) of a given type

   @param type of choice to get
   @return choice of given type
   *
  Collection<Choice> getChoicesOfType(ProgramType type);

  /**
   Get Chords

   @return Chords
   *
  Collection<SegmentChord> getChords();

  /**
     Get content of super entity, comprising many sub entities

     @return super entity content
     *
  SegmentContent getContent();

  /**
   Get density of segment

   @return density
   *
  Double getDensity();

  /**
   Get end-at time for Segment

   @return end-at time
   *
  Instant getEndAt();

  /**
   Get key for the segment

   @return key
   *
  String getKey();

  /**
   Get Memes

   @return Memes
   *
  Collection<SegmentMeme> getMemes();

  /**
   Get Messages

   @return Messages
   *
  Collection<SegmentMessage> getMessages();

  /**
   Get offset

   @return offset
   *
  Long getOffset();

  /**
   Get Picks

   @return Picks
   *
  Collection<Pick> getPicks();

  /**
   get offset of previous segment

   @return previous segment offset
   *
  Long getPreviousOffset() throws CoreException;

  /**
   Gtr ing

   @return ing
   *
  Map<String, Object> getReport();

  /**
   Get state of Segment

   @return state
   *
  SegmentState getState();

  /**
   Get tempo of segment

   @return tempo
   *
  Double getTempo();

  /**
   Get the total length of the segment

   @return total length
   *
  Integer getTotal();

  /**
   Get Type

   @return Type
   *
  FabricatorType getType();

  /**
   Get waveform key of segment

   @return waveform key
   *
  String getWaveformKey();

  /**
   Whether this Segment is at offset 0

   @return true if offset is 0
   *
  boolean isInitial();

  /**
   Put a key/value into the report

   @param key to put into report
   *
  void putReport(String key, String value);

  /**
   Set Arrangements
   + If there are any exceptions, store them in the SuperEntity errors

   @param arrangements to set
   *
  void setArrangements(Collection<Arrangement> arrangements);

  /**
   Set begin-at time for Segment

   @param beginAt time to set
   @return this Segment (for chaining setters)
   *
  Segment setBeginAt(String beginAt);

  /**
   Set end-at time for Segment

   @param beginAt time to set
   @return this Segment (for chaining setters)
   *
  Segment setBeginAtInstant(Instant beginAt);

  /**
   Set Chain ID

   @param chainId to set
   @return this Segment (for chaining setters)
   *
  Segment setChainId(UUID chainId);

  /**
   Set Choices; copy in contents, to preserve mutability of data persistent internally for this class.
   + If there are any exceptions, store them in the SuperEntity errors

   @param choices to set
   *
  void setChoices(Collection<Choice> choices);

  /**
   Set Chords
   + If there are any exceptions, store them in the SuperEntity errors

   @param chords to set
   *
  void setChords(Collection<SegmentChord> chords);

  /**
     Set JSON string content (comprising many sub entities) of super entity

     @param json to set
     @return this super entity (for chaining setters)
     @throws CoreException on bad JSON
     *
  Segment setContent(String json) throws CoreException;

  /**
   Set density of segment

   @param density to set
   @return this Segment (for chaining setters)
   *
  Segment setDensity(Double density);

  /**
   Set end-at time for Segment

   @param endAt time to set
   @return this Segment (for chaining setters)
   *
  Segment setEndAt(String endAt);

  /**
   Set end-at time of Segment

   @param endAt time to set
   @return this Segment (for chaining setters)
   *
  Segment setEndAtInstant(Instant endAt);

  /**
   Set the key for the segment

   @param key to set
   @return this Segment (for chaining setters)
   *
  Segment setKey(String key);

  /**
   Set Memes

   @param memes to set
   *
  void setMemes(Collection<SegmentMeme> memes);

  /**
   Set Messages

   @param messages to set
   *
  void setMessages(Collection<SegmentMessage> messages);

  /**
   Set offset of segment

   @param offset to set
   @return this Segment (for chaining setters)
   *
  Segment setOffset(Long offset);

  /**
   Set Picks

   @param picks to set
   *
  void setPicks(Collection<Pick> picks);

  /**
   Set Report

   @param input to set
   *
  void setReport(Map<String, Object> input);

  /**
   Set state of Segment

   @param value to set
   @return this Segment (for chaining setters)
   *
  Segment setState(String value);

  /**
   Set state of Segment

   @param value to set
   @return this Segment (for chaining setters)
   *
  Segment setStateEnum(SegmentState value);

  /**
   Set tempo of segment

   @param tempo to set
   @return this Segment (for chaining setters)
   *
  Segment setTempo(Double tempo);

  /**
   Set total length of segment

   @param total to set
   @return this Segment (for chaining setters)
   *
  Segment setTotal(Integer total);

  /**
   Set Type

   @param type to set
   *
  void setType(String type);

  /**
   Set TypeEnum

   @param type to set
   *
  void setTypeEnum(FabricatorType type);

  /**
   Set waveform key of segment

   @param waveformKey to set
   @return this Segment (for chaining setters)
   *
  Segment setWaveformKey(String waveformKey);

  /**
     Set createdat time

     @param createdAt time
     @return entity
     *
  Segment setCreatedAt(String createdAt);

  /**
     Set createdat time

     @param createdAt time
     @return entity
     *
  Segment setCreatedAtInstant(Instant createdAt);

  /**
     Set updated-at time

     @param updatedAt time
     @return entity
     *
  Segment setUpdatedAt(String updatedAt);

  /**
     Set updated-at time

     @param updatedAt time
     @return entity
     *
  Segment setUpdatedAtInstant(Instant updatedAt);

  /**
   Validate that all entities have an id,
   that none of the entities provided share an id, and that relation ids are OK

   @return this super entity (for chaining setters)
   @throws CoreException if invalid attributes, or child entities have duplicate ids or bad relations are detected
   *
  SuperEntity validateContent() throws CoreException;

  /**
   Get ofd-at instant

   @return ofd-at instant
   *
  Instant getCreatedAt();


  /**
   Get parent id

   @return parent id
   *
  UUID getParentId();

  /**
   Get updated-at time

   @return updated-at time
   *
  Instant getUpdatedAt();

  /**
   Set entity id

   @param id to set
   @return entity
   *
  Entity setId(UUID id);

  /**
   Add an exception to the SuperEntity errors

   @param exception to add
   *
  void add(CoreException exception);

  /**
   Whether this resource belongs to the specified resource

   @param resource to test whether this belongs to
   @return true if this belongs to the specified resource
   *
  boolean belongsTo(Entity resource);

  /**
   Consume all data of a payload:
   + Set all attributes
   + Adding any available sub-entities
   + Re-index relationships and prune orphaned entities

   @param payload to consume
   @return this Entity (for chaining methods)
   @throws CoreException on failure to consume payload
   *
  <N extends Entity> N consume(Payload payload) throws CoreException;

  /**
   Set all attributes of entity of a payload object
   <p>
   There's a default implementation in EntityImpl, which uses the attribute names to compute setter method names,
   and maps all value objects to setters. Simple entities need not override this method.
   <p>
   However, entities with relationships ought to override the base method, invoke the super, then parse additionally:
   |
   |  @Override
   |  public PayloadObject toResourceObject() {
   |    return super.toResourceObject()
   |      .add("account", ResourceRelationship.of("accounts", accountId));
   |  }
   |

   @param payloadObject of which to get attributes
   @return this Entity (for chaining methods)
   @throws CoreException on failure to set
   *
  <N extends Entity> N consume(PayloadObject payloadObject) throws CoreException;

  /**
   Get a value of a target object via attribute name

   @param name of attribute to get
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   *
  Optional<Object> get(String name) throws InvocationTargetException, IllegalAccessException, CoreException;

  /**
   Get a value of a target object via getter method

   @param getter to use
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   *
  Optional<Object> get(Method getter) throws InvocationTargetException, IllegalAccessException;

  /**
   Get errors

   @return errors
   *
  Collection<CoreException> getErrors();

  /**
   Get a collection of resource attribute names

   @return resource attribute names
   *
  ImmutableList<String> getResourceAttributeNames();

  /**
   Get resource attributes based on getResourceAttributeNames() for this instance
   NOTE: this is implemented in EntityImpl and widely shared; individual classes simply return getResourceAttributeNames()

   @return payload attributes
   *
  Map<String, Object> getResourceAttributes();

  /**
   Get this resource's belongs-to relations

   @return list of classes this resource belongs to
   *
  ImmutableList<Class> getResourceBelongsTo();

  /**
   Get this resource's has-many relations

   @return list of classes this resource has many of
   *
  ImmutableList<Class> getResourceHasMany();

  /**
   get Entity ID
   <p>
   For SuperEntity, that's a UUID
   <p>
   For SubEntity, that's a UUID

   @return Entity Id
   *
  String getResourceId();

  /**
   get Entity Type- always a plural noun, i.e. Users or Libraries

   @return Entity Type
   *
  String getResourceType();

  /**
   Get the URI of any entity

   @return Entity URI
   *
  URI getURI();

  /**
   Set all values available of a source Entity

   @param of source Entity
   *
  void setAllResourceAttributes(Entity of);

  /**
   Set a value using a setter method

   @param method setter to use
   @param value  to set
   *
  void set(Method method, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

  /**
   Set a value using an attribute name

   @param name  of attribute for which to find setter method
   @param value to set
   *
  void set(String name, Object value) throws CoreException;

  /**
   Shortcut to build payload object with no child entities

   @return resource object
   *
  PayloadObject toPayloadObject();

  /**
   Build and return a Entity Object of this entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the attribute names to compute getter method names,
   and maps all attribute names to value objects. Simple entities need not override this method.
   <p>
   However, entities with relationships ought to override the base method, get the super result, then add to it, e.g.:
   |
   |  @Override
   |  public PayloadObject toPayloadObject() {
   |    return super.toPayloadObject()
   |      .add("account", ResourceRelationship.of("accounts", accountId));
   |  }
   |
   <p>
   Also, receives an (optionally, empty) collection of potential child resources-- only match resources are added

   @param childResources to search for possible children-- only add matched resources
   @return resource object
   *
  <N extends Entity> PayloadObject toPayloadObject(Collection<N> childResources);

  /**
   Build and return a reference (type and id only) Entity Object of this entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the resource type and id

   @return resource object
   *
  PayloadObject toPayloadReferenceObject();

  /**
   Validate data.

   @return this Entity (for chaining methods)
   @throws CoreException if invalid.
   *
  <N extends Entity> N validate() throws CoreException;

   */
}
