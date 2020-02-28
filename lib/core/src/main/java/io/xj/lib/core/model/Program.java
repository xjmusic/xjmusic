// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

import java.time.Instant;
import java.util.UUID;

public class Program extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES =
    ImmutableList.<String>builder()
      .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
      .add("state")
      .add("key")
      .add("tempo")
      .add("type")
      .add("name")
      .add("density")
      .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(User.class)
    .add(Library.class)
    .build();
  public static ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(ProgramMeme.class)
    .add(ProgramSequence.class)
    .add(ProgramSequenceChord.class)
    .add(ProgramSequencePattern.class)
    .add(ProgramVoiceTrack.class)
    .add(ProgramSequencePatternEvent.class)
    .add(ProgramSequenceBinding.class)
    .add(ProgramSequenceBindingMeme.class)
    .add(ProgramVoice.class)
    .build();
  private UUID userId;
  private UUID libraryId;
  private ProgramState state;
  private String key;
  private Double tempo;
  private ProgramType type;
  private String name;
  private Exception stateException;
  private Exception typeException;
  private Double density;

  /**
   Create a new Program

   @return new Program
   */
  public static Program create() {
    return new Program()
      .setId(UUID.randomUUID());
  }

  /**
   Create a new Program

   @return new program
   @param user    of program
   @param library of program
   @param type    of program
   @param state   of program
   @param name    of program
   @param key     of program
   @param tempo   of program
   @param density of program
   */
  public static Program create(User user, Library library, ProgramType type, ProgramState state, String name, String key, double tempo, double density) {
    return create()
      .setUserId(user.getId())
      .setLibraryId(library.getId())
      .setTypeEnum(type)
      .setStateEnum(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density);
  }

  /**
   Create a new Program

   @return new program
   @param user    of program
   @param library of program
   @param type    of program
   @param state   of program
   @param name    of program
   @param key     of program
   @param tempo   of program
   @param density of program
   */
  public static Program create(User user, Library library, String type, String state, String name, String key, double tempo, double density) {
    return create()
      .setUserId(user.getId())
      .setLibraryId(library.getId())
      .setType(type)
      .setState(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density);
  }

  /**
   Get key for the program

   @return key
   */
  public String getKey() {
    return key;
  }

  /**
   Get Library ID

   @return library id
   */
  public UUID getLibraryId() {
    return libraryId;
  }

  /**
   Get name for the program

   @return name
   */
  public String getName() {
    return name;
  }

  /**
   Get parent id

   @return parent id
   */
  @Override
  public UUID getParentId() {
    return libraryId;
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
   Get state of Program

   @return state
   */
  public ProgramState getState() {
    return state;
  }

  /**
   Get tempo of program

   @return tempo
   */
  public Double getTempo() {
    return tempo;
  }

  /**
   Get Type

   @return Type
   */
  public ProgramType getType() {
    return type;
  }

  /**
   Get User

   @return User
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Program setCreatedAt(String createdAt) {
    super.setCreatedAt(createdAt);
    return this;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Program setCreatedAtInstant(Instant createdAt) {
    super.setCreatedAtInstant(createdAt);
    return this;
  }

  /**
   Set the id for the program

   @param id to set
   @return this Program (for chaining methods)
   */
  @Override
  public Program setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   Set the density for the program

   @param density to set
   @return this Program (for chaining methods)
   */
  public Program setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   Set the key for the program

   @param key to set
   @return this Program (for chaining methods)
   */
  public Program setKey(String key) {
    this.key = key;
    return this;
  }

  /**
   Set Library ID

   @param libraryId to set
   @return this Program (for chaining methods)
   */
  public Program setLibraryId(UUID libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  /**
   Set name of program

   @param name to set
   @return this Program (for chaining methods)
   */
  public Program setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set state of Program

   @param value to set
   @return this Program (for chaining methods)
   */
  public Program setState(String value) {
    try {
      state = ProgramState.validate(value);
    } catch (CoreException e) {
      stateException = e;
    }
    return this;
  }

  /**
   Set state of Program

   @param value to set
   @return this Program (for chaining methods)
   */
  public Program setStateEnum(ProgramState value) {
    state = value;
    return this;
  }

  /**
   Set tempo of program

   @param tempo to set
   @return this Program (for chaining methods)
   */
  public Program setTempo(Double tempo) {
    this.tempo = tempo;
    return this;
  }

  /**
   Set Type

   @param type to set
   @return this Program (for chaining methods)
   */
  public Program setType(String type) {
    try {
      this.type = ProgramType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   Set TypeEnum

   @param type to set
   @return this Program (for chaining methods)
   */
  public Program setTypeEnum(ProgramType type) {
    this.type = type;
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Program setUpdatedAt(String updatedAt) {
    super.setUpdatedAt(updatedAt);
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Program setUpdatedAtInstant(Instant updatedAt) {
    super.setUpdatedAtInstant(updatedAt);
    return this;
  }

  /**
   Set User ID

   @param userId to set
   @return this Program (for chaining methods)
   */
  public Program setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  /**
   Validate data.

   @throws CoreException if invalid.
   */
  public void validate() throws CoreException {
    require(userId, "User ID");
    require(libraryId, "Library ID");
    require(name, "Name");
    require(key, "Key");
    requireNonZero(tempo, "Key");

    requireNo(typeException, "Type");
    require(type, "Type");

    requireNo(stateException, "State");
    require(state, "State");
  }

  public Double getDensity() {
    return density;
  }

  /*

  TODO remove Program legacy superentity methods

  /**
   Validate that all entities have an id,
   that none of the entities provided share an id, and that relation ids are OK

   @return this super entity (for chaining setters)
   @throws CoreException if invalid attributes, or child entities have duplicate ids or bad relations are detected
   *
  public Program validateContent() throws CoreException {
    SubEntity.validate(this.getAllSubEntities());
    for (Pattern pattern : getPatterns()) ensureRelations(pattern);
    for (Track track : getTracks()) ensureRelations(track);
    for (Event event : getEvents()) ensureRelations(event);
    for (SequenceBinding sequenceBinding : getSequenceBindings()) ensureRelations(sequenceBinding);
    for (SequenceBindingMeme sequenceBindingMeme : getSequenceBindingMemes()) ensureRelations(sequenceBindingMeme);
    for (SequenceChord sequenceChord : getSequenceChords()) ensureRelations(sequenceChord);
    return this;
  }

  /**
   Ensure that an Pattern relates to an existing Sequence stored in the Program

   @param pattern to ensure existing relations of
   @throws CoreException if no such Sequence exists
   *
  private void ensureRelations(Pattern pattern) throws CoreException {
    if (Objects.isNull(pattern.getProgramSequenceId()))
      throw new CoreException(String.format("Pattern id=%s has null sequenceId", pattern.getId()));

    if (!sequenceMap.containsKey(pattern.getProgramSequenceId()))
      throw new CoreException(String.format("Pattern id=%s has nonexistent sequenceId=%s", pattern.getId(), pattern.getProgramSequenceId()));

    if (Objects.isNull(pattern.getProgramVoiceId()))
      throw new CoreException(String.format("Pattern id=%s has null voiceId", pattern.getId()));

    if (!voiceMap.containsKey(pattern.getProgramVoiceId()))
      throw new CoreException(String.format("Pattern id=%s has nonexistent voiceId=%s", pattern.getId(), pattern.getProgramVoiceId()));
  }

  /**
   Ensure that an Track relates to an existing Sequence stored in the Program

   @param track to ensure existing relations of
   @throws CoreException if no such Sequence exists
   *
  private void ensureRelations(Track track) throws CoreException {
    if (Objects.isNull(track.getProgramVoiceId()))
      throw new CoreException(String.format("Track id=%s has null voiceId", track.getId()));

    if (!voiceMap.containsKey(track.getProgramVoiceId()))
      throw new CoreException(String.format("Track id=%s has nonexistent voiceId=%s", track.getId(), track.getProgramVoiceId()));
  }

  /**
   Ensure that an SequenceBinding relates to an existing Sequence stored in the Program

   @param sequenceBinding to ensure existing relations of
   @throws CoreException if no such Sequence exists
   *
  private void ensureRelations(SequenceBinding sequenceBinding) throws CoreException {
    if (Objects.isNull(sequenceBinding.getProgramSequenceId()))
      throw new CoreException(String.format("SequenceBinding id=%s has null sequenceId", sequenceBinding.getId()));

    if (!sequenceMap.containsKey(sequenceBinding.getProgramSequenceId()))
      throw new CoreException(String.format("SequenceBinding id=%s has nonexistent sequenceId=%s", sequenceBinding.getId(), sequenceBinding.getProgramSequenceId()));
  }

  /**
   Ensure that an SequenceChord relates to an existing Sequence stored in the Program

   @param sequenceChord to ensure existing relations of
   @throws CoreException if no such Sequence exists
   *
  private void ensureRelations(SequenceChord sequenceChord) throws CoreException {
    if (Objects.isNull(sequenceChord.getProgramSequenceId()))
      throw new CoreException(String.format("SequenceChord id=%s has null sequenceId", sequenceChord.getId()));

    if (!sequenceMap.containsKey(sequenceChord.getProgramSequenceId()))
      throw new CoreException(String.format("SequenceChord id=%s has nonexistent sequenceId=%s", sequenceChord.getId(), sequenceChord.getProgramSequenceId()));
  }

  /**
   Ensure that an SequenceBindingMeme relates to an existing SequenceBinding stored in the Program

   @param sequenceBindingMeme to ensure existing relations of
   @throws CoreException if no such SequenceBinding exists
   *
  private void ensureRelations(SequenceBindingMeme sequenceBindingMeme) throws CoreException {
    if (Objects.isNull(sequenceBindingMeme.getProgramSequenceBindingId()))
      throw new CoreException(String.format("SequenceBindingMeme id=%s has null sequenceBindingId", sequenceBindingMeme.getId()));

    if (!sequenceBindingMap.containsKey(sequenceBindingMeme.getProgramSequenceBindingId()))
      throw new CoreException(String.format("SequenceBindingMeme id=%s has nonexistent sequenceBindingId=%s", sequenceBindingMeme.getId(), sequenceBindingMeme.getProgramSequenceBindingId()));
  }

  /**
   Ensure that an Event relates to an existing Pattern and Voice stored in the Program

   @param event to ensure existing relations of
   @throws CoreException if no such Pattern exists
   *
  private void ensureRelations(Event event) throws CoreException {
    if (Objects.isNull(event.getProgramSequencePatternId()))
      throw new CoreException(String.format("Event id=%s has null patternId", event.getId()));

    if (!patternMap.containsKey(event.getProgramSequencePatternId()))
      throw new CoreException(String.format("Event id=%s has nonexistent patternId=%s", event.getId(), event.getProgramSequencePatternId()));
  }


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

  */

  /*

  TODO remove Program legacy interface methods


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
   Get all entities contained within this entity.
   Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

   @return collection of entities
   *
  Collection<SubEntity> getAllSubEntities();

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

  /*
   TODO review if this is needed


   /**
   Format a comma-separated list of entity counts of a collection of entities

   @param entities for format a comma-separated list of the # occurrences of each class
   @return comma-separated list in text
   *
  public static <N extends Entity> String histogramString(Collection<N> entities) {
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
  public static String toKeyValueString(String name, ImmutableMap<String, String> properties) {
    return String.format("%s{%s}", name, CSV.of(properties));
  }

   */

/*
   TODO remove Program legacy superentity methods

  /**
   Add an Pattern to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param pattern to add
   @return Pattern with newly added unique id
   *
  public Pattern add(Pattern pattern) {
    try {
      requireId("before adding Pattern");
      pattern.setProgramId(getId());
      ensureRelations(pattern);
      return SubEntity.add(patternMap, pattern);
    } catch (CoreException e) {
      add(e);
      return pattern;
    }
  }

  /**
   Add an Track to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param track to add
   @return Track with newly added unique id
   *
  public Track add(Track track) {
    try {
      requireId("before adding Track");
      track.setProgramId(getId());
      ensureRelations(track);
      return SubEntity.add(trackMap, track);
    } catch (CoreException e) {
      add(e);
      return track;
    }
  }

  /**
   Add a Event to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param event to add
   @return Event with newly added unique id
   *
  public Event add(Event event) {
    try {
      requireId("before adding Event");
      event.setProgramId(getId());
      ensureRelations(event);
      return SubEntity.add(eventMap, event);
    } catch (CoreException e) {
      add(e);
      return event;
    }
  }

  /**
   Add a ProgramMeme to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param meme to add
   @return ProgramMeme with newly added unique id
   *
  public ProgramMeme add(ProgramMeme meme) {
    try {
      requireId("before adding ProgramMeme");
      meme.setProgramId(getId());
      return SubEntity.add(memeMap, meme);
    } catch (CoreException e) {
      add(e);
      return meme;
    }
  }

  /**
   Add a Sequence to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequence to add
   @return Sequence with newly added unique id
   *
  public Sequence add(Sequence sequence) {
    try {
      requireId("before adding Sequence");
      sequence.setProgramId(getId());
      return SubEntity.add(sequenceMap, sequence);
    } catch (CoreException e) {
      add(e);
      return sequence;
    }
  }

  /**
   Add a Sequence Binding to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBinding to add
   @return SequenceBinding with newly added unique id
   *
  public SequenceBinding add(SequenceBinding sequenceBinding) {
    try {
      requireId("before adding SequenceBinding");
      sequenceBinding.setProgramId(getId());
      ensureRelations(sequenceBinding);
      return SubEntity.add(sequenceBindingMap, sequenceBinding);
    } catch (CoreException e) {
      add(e);
      return sequenceBinding;
    }
  }

  /**
   Add a Sequence Binding MemeEntity to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBindingMeme to add
   @return SequenceBindingMeme with newly added unique id
   *
  public SequenceBindingMeme add(SequenceBindingMeme sequenceBindingMeme) {
    try {
      requireId("before adding SequenceBindingMeme");
      sequenceBindingMeme.setProgramId(getId());
      ensureRelations(sequenceBindingMeme);
      return SubEntity.add(sequenceBindingMemeMap, sequenceBindingMeme);
    } catch (CoreException e) {
      add(e);
      return sequenceBindingMeme;
    }
  }

  /**
   Add a SequenceChord to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param chord to add
   @return SequenceChord with newly added unique id
   *
  public SequenceChord add(SequenceChord sequenceChord) {
    try {
      requireId("before adding SequenceChord");
      sequenceChord.setProgramId(getId());
      ensureRelations(sequenceChord);
      return SubEntity.add(sequenceChordMap, sequenceChord);
    } catch (CoreException e) {
      add(e);
      return sequenceChord;
    }
  }

  /**
   Add an Voice to Program
   + If there are any exceptions, store them in the SuperEntity errors

   @param voice to add
   @return Voice with newly added unique id
   *
  public Voice add(Voice voice) {
    try {
      requireId("before adding Voice");
      voice.setProgramId(getId());
      return SubEntity.add(voiceMap, voice);
    } catch (CoreException e) {
      add(e);
      return voice;
    }
  }

    /**
   Get all entities contained within this entity.
   Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

   @return collection of entities

  public Collection<SubEntity> getAllSubEntities() {
    Collection<SubEntity> out = Lists.newArrayList();
    out.addAll(getMemes());
    out.addAll(getEvents());
    out.addAll(getPatterns());
    out.addAll(getTracks());
    out.addAll(getSequenceBindingMemes());
    out.addAll(getSequenceBindings());
    out.addAll(getSequenceChords());
    out.addAll(getSequences());
    out.addAll(getVoices());
    return out;
  }

  /**
   Consume all data of a payload:
   + Set all attributes
   + Adding any available sub-entities
   + Re-index relationships and prune orphaned entities

   @param payload to consume
   @return this Entity (for chaining methods)
   @throws CoreException on failure to consume payload
   *
  public Program consume(Payload payload) throws CoreException {
    super.consume(payload);
    syncSubEntities(payload, memeMap, ProgramMeme.class);
    syncSubEntities(payload, voiceMap, Voice.class);
    syncSubEntities(payload, sequenceMap, Sequence.class);
    syncSubEntities(payload, sequenceChordMap, SequenceChord.class); // requires Sequence
    syncSubEntities(payload, sequenceBindingMap, SequenceBinding.class); // requires Sequence
    syncSubEntities(payload, sequenceBindingMemeMap, SequenceBindingMeme.class); // requires SequenceBinding
    syncSubEntities(payload, patternMap, Pattern.class); // requires Sequence, Voice
    syncSubEntities(payload, trackMap, Track.class); // requires Sequence, Voice
    syncSubEntities(payload, eventMap, Event.class); // requires Pattern
    return this;
  }


  /**
   Get all Chords for a given sequence

   @param sequence to get chords for
   @return chords of given sequence
   *
  public Collection<SequenceChord> getChords(Sequence sequence) {
    return getChordsOfSequence(sequence.getId());
  }

  /**
   Get content of super entity, comprising many sub entities

   @return super entity content
   *
  public ProgramContent getContent() {
    return ProgramContent.of(this);
  }

  /**
   Get (computed) density of program
   Program doesn't actually have density property; it's computed by averaging the density of all its sub entities

   @return density
   *
  public Double getDensity() {
    double DT = 0;
    double T = 0;
    for (Sequence sequence : getSequences()) {
      DT += sequence.getDensity() * sequence.getTotal();
      T += sequence.getTotal();
    }
    return 0 < T ? DT / T : 0;
  }

  /**
   Get events for a specified pattern

   @param pattern to get events for
   @return events
   *
  public Collection<Event> getEventsForPattern(Pattern pattern) {
    return getEvents().stream()
      .filter(event -> pattern.getId().equals(event.getProgramSequencePatternId()))
      .collect(Collectors.toList());
  }

  /**
   Get Memes

   @return Memes
   *
  public Collection<ProgramMeme> getMemes() {
    return memeMap.values();
  }

  /**
   Get all Sequence Binding Memes for a given Sequence Binding

   @param sequenceBinding to get memes for
   @return sequence binding memes
   *
  public Collection<SequenceBindingMeme> getMemes(SequenceBinding sequenceBinding) {
    return getSequenceBindingMemes().stream()
      .filter(meme -> meme.getProgramSequenceBindingId().equals(sequenceBinding.getId()))
      .collect(Collectors.toList());
  }


  /**
   Get a Pattern by its UUID

   @param id of pattern to get
   @return Pattern
   @throws CoreException if no pattern is found with that id
   *
  public Pattern getPattern(UUID id) throws CoreException {
    if (!patternMap.containsKey(id))
      throw new CoreException(String.format("Found no Pattern id=%s", id));
    return patternMap.get(id);
  }

  /**
   Get Events

   @return Events
   *
  public Collection<Event> getEvents() {
    return eventMap.values();
  }

  /**
   Get Patterns

   @return Patterns
   *
  public Collection<Pattern> getPatterns() {
    return patternMap.values();
  }

  /**
   Get a Track by its UUID

   @param id of track to get
   @return Track
   @throws CoreException if no track is found with that id
   *
  public Track getTrack(UUID id) throws CoreException {
    if (!trackMap.containsKey(id))
      throw new CoreException(String.format("Found no Track id=%s", id));
    return trackMap.get(id);
  }

  /**
   Get Tracks

   @return Tracks
   *
  public Collection<Track> getTracks() {
    return trackMap.values();
  }

  /**
   Get a sequence by its UUID

   @param id to get
   @return Sequence
   *
  public Sequence getSequence(UUID id) throws CoreException {
    if (!sequenceMap.containsKey(id))
      throw new CoreException(String.format("Found no Sequence id=%s", id));
    return sequenceMap.get(id);
  }

  /**
   Get Sequence Binding Memes

   @return Sequence Binding Memes
   *
  public Collection<SequenceBindingMeme> getSequenceBindingMemes() {
    return sequenceBindingMemeMap.values();
  }

  /**
   Get Sequence Bindings

   @return Sequence Bindings
   *
  public Collection<SequenceBinding> getSequenceBindings() {
    return sequenceBindingMap.values();
  }

  /**
   Get Chords

   @return Chords
   *
  public Collection<SequenceChord> getSequenceChords() {
    return sequenceChordMap.values();
  }

  /**
   Get Sequences

   @return Sequences
   *
  public Collection<Sequence> getSequences() {
    return sequenceMap.values();
  }

  /**
   Get Voices

   @return Voices
   *
  public Collection<Voice> getVoices() {
    return voiceMap.values();
  }


  /**
   Set JSON string content (comprising many sub entities) of super entity

   @param json to set
   @return this super entity (for chaining setters)
   @throws CoreException on bad JSON
   *
  public Program setContent(String json) {
    ProgramContent content = gsonProvider.gson().fromJson(json, ProgramContent.class);
    setMemes(content.getMemes());
    setSequences(content.getSequences());
    setSequenceBindings(content.getSequenceBindings());
    setSequenceBindingMemes(content.getSequenceBindingMemes());
    setSequenceChords(content.getSequenceChords());
    setVoices(content.getVoices());
    setPatterns(content.getPatterns());
    setTracks(content.getTracks());
    setPatternEvents(content.getEvents());
    return this;
  }

  /**
   Set all content of program, cloned of another source program, with all new UUID, preserving relationships.

   @param of program
   @return this Program (for chaining methods)
   *
  public Program setContentCloned(Program of) {
    setMemes(of.getMemes());
    setVoices(of.getVoices());
    setSequences(of.getSequences());
    setPatterns(of.getPatterns()); // after sequences, voices
    setTracks(of.getTracks()); // after sequences, voices
    setSequenceBindings(of.getSequenceBindings()); // after sequences
    setSequenceBindingMemes(of.getSequenceBindingMemes()); // after sequence bindings
    setSequenceChords(of.getSequenceChords()); // after sequences
    setPatternEvents(of.getEvents()); // after patterns
    return this;
  }

  /**
   Set Memes
   + If there are any exceptions, store them in the SuperEntity errors

   @param memes to set
   @return this Program (for chaining methods)
   *
  public Program setMemes(Collection<ProgramMeme> memes) {
    memeMap.clear();
    for (ProgramMeme meme : memes) {
      add(meme);
    }
    return this;
  }

  /**
   Set PatternEvents
   + If there are any exceptions, store them in the SuperEntity errors

   @param events to set
   @return this Program (for chaining methods)
   *
  public Program setPatternEvents(Collection<Event> events) {
    eventMap.clear();
    for (Event event : events) {
      add(event);
    }
    return this;
  }

  /**
   Set Patterns
   + If there are any exceptions, store them in the SuperEntity errors

   @param patterns to set
   @return this Program (for chaining methods)
   *
  public Program setPatterns(Collection<Pattern> patterns) {
    patternMap.clear();
    for (Pattern pattern : patterns) {
      add(pattern);
    }
    return this;
  }

  /**
   Set Tracks
   + If there are any exceptions, store them in the SuperEntity errors

   @param tracks to set
   @return this Program (for chaining methods)
   *
  public Program setTracks(Collection<Track> tracks) {
    trackMap.clear();
    for (Track track : tracks) {
      add(track);
    }
    return this;
  }

  /**
   Set all Sequence binding memes
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBindingMemes to set
   @return this Program (for chaining methods)
   *
  public Program setSequenceBindingMemes(Collection<SequenceBindingMeme> sequenceBindingMemes) {
    sequenceBindingMemeMap.clear();
    for (SequenceBindingMeme sequenceBindingMeme : sequenceBindingMemes) {
      add(sequenceBindingMeme);
    }
    return this;
  }

  /**
   Set all Sequence bindings
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceBindings to set
   @return this Program (for chaining methods)
   *
  public Program setSequenceBindings(Collection<SequenceBinding> sequenceBindings) {
    sequenceBindingMap.clear();
    for (SequenceBinding sequenceBinding : sequenceBindings) {
      add(sequenceBinding);
    }
    return this;
  }

  /**
   Set all Sequence chords
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequenceChords to set
   @return this Program (for chaining methods)
   *
  public Program setSequenceChords(Collection<SequenceChord> chords) {
    sequenceChordMap.clear();
    for (SequenceChord chord : chords) {
      add(chord);
    }
    return this;
  }

  /**
   Set Sequences; copy in contents, to preserve mutability of data persistent internally for this class.
   + If there are any exceptions, store them in the SuperEntity errors

   @param sequences to set
   @return this Program (for chaining methods)
   *
  public Program setSequences(Collection<Sequence> sequences) {
    sequenceMap.clear();
    for (Sequence sequence : sequences) {
      add(sequence);
    }
    return this;
  }

  /**
   Set all voices
   + If there are any exceptions, store them in the SuperEntity errors

   @param voices to set
   @return this Program (for chaining methods)
   *
  public Program setVoices(Collection<Voice> voices) {
    voiceMap.clear();
    for (Voice voice : voices) {
      add(voice);
    }
    return this;
  }


  /**
   Get all available sequence pattern offsets of a given sequence

   @param sequenceBinding to get available sequence pattern offsets for
   @return collection of available sequence pattern offsets
   *
public Collection<Long> getAvailableOffsets(SequenceBinding sequenceBinding) {
  return getSequenceBindings().stream()
    .map(SequenceBinding::getOffset)
    .distinct()
    .collect(Collectors.toList());
}

*/

}
