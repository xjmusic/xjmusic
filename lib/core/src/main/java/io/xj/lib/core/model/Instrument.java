// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

import java.util.UUID;

public class Instrument extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("state")
    .add("type")
    .add("name")
    .add("density")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(User.class)
    .add(Library.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(InstrumentAudio.class)
    .add(InstrumentMeme.class)
    .build();

  private InstrumentState state;
  private String name;
  private InstrumentType type;
  private UUID userId;
  private UUID libraryId;
  private Exception stateException;
  private Exception typeException;
  private Double density;

  /**
   Create a new instrument

   @return new instrument
   */
  public static Instrument create() {
    return new Instrument()
      .setDensity(1.0)
      .setId(UUID.randomUUID());
  }

  /**
   Create a new instrument

   @param user    of instrument
   @param library of instrument
   @param type    of instrument
   @param state   of instrument
   @param name    of instrument
   @return new instrument
   */
  public static Instrument create(User user, Library library, InstrumentType type, InstrumentState state, String name) {
    return create(library)
      .setUserId(user.getId())
      .setTypeEnum(type)
      .setStateEnum(state)
      .setName(name);
  }

  /**
   Create a new instrument

   @param user    of instrument
   @param library of instrument
   @param type    of instrument
   @param state   of instrument
   @param name    of instrument
   @param density of instrument
   @return new instrument
   */
  public static Instrument create(User user, Library library, String type, String state, String name, Double density) {
    return create(library)
      .setUserId(user.getId())
      .setType(type)
      .setState(state)
      .setDensity(density)
      .setName(name);
  }

  /**
   Create a new instrument

   @param library of instrument
   @return new instrument
   */
  public static Instrument create(Library library) {
    return create()
      .setLibraryId(library.getId());
  }

  /*
  TODO address cloning instrument
  @Override
  public Instrument setContentCloned(Instrument of) {
    setMemes(of.getMemes());
    setAudios(of.getAudios());
    setAudioEvents(of.getAudioEvents());
    setAudioChords(of.getAudioChords());
    return this;
  }
*/

/*
TODO address density computation for instrument
  public Double getDensity() {
    double DL = 0;
    double L = 0;
    for (Audio audio : getAudios()) {
      DL += audio.getDensity() * audio.getLength();
      L += audio.getLength();
    }
    return 0 < L ? DL / L : 0;
  }
  */

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
   get Density

   @return Density
   */
  public Double getDensity() {
    return density;
  }

  /**
   get Name

   @return Name
   */
  public String getName() {
    return name;
  }

  /**
   get LibraryId

   @return LibraryId
   */
  public UUID getLibraryId() {
    return libraryId;
  }

  /**
   get ParentId

   @return ParentId
   */
  public UUID getParentId() {
    return libraryId;
  }

  /**
   get State

   @return State
   */
  public InstrumentState getState() {
    return state;
  }

  /**
   get Type

   @return Type
   */
  public InstrumentType getType() {
    return type;
  }

  /**
   get UserId

   @return UserId
   */
  public UUID getUserId() {
    return userId;
  }

  /**
   set Name

   @param name to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set id

   @param id to set
   @return this Instrument (for chaining setters)
   */
  @Override
  public Instrument setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   set Density

   @param density to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setDensity(Double density) {
    this.density = density;
    return this;
  }

  /**
   set LibraryId

   @param libraryId to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setLibraryId(UUID libraryId) {
    this.libraryId = libraryId;
    return this;
  }

  /**
   set State

   @param state to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setState(String state) {
    try {
      this.state = InstrumentState.validate(state);
    } catch (CoreException e) {
      stateException = e;
    }
    return this;
  }

  /**
   set StateEnum

   @param state to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setStateEnum(InstrumentState state) {
    this.state = state;
    return this;
  }

  /**
   set Type

   @param type to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setType(String type) {
    try {
      this.type = InstrumentType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setTypeEnum(InstrumentType type) {
    this.type = type;
    return this;
  }

  /**
   set UserId

   @param userId to set
   @return this Instrument (for chaining setters)
   */
  public Instrument setUserId(UUID userId) {
    this.userId = userId;
    return this;
  }

  @Override
  public String toString() {
    return name + " " + "(" + type + ")";
  }

  @Override
  public void validate() throws CoreException {
    require(userId, "User ID");
    require(libraryId, "Library ID");
    require(name, "Name");

    requireNo(typeException, "Type");
    require(type, "Type");

    requireNo(stateException, "State");
    require(state, "State");
  }

















/*
TODO remove legacy superentity code


  @Override
  public InstrumentMeme add(InstrumentMeme meme) {
    try {
      requireId("before adding InstrumentMeme");
      meme.setInstrumentId(getId());
      return SubEntity.add(memeMap, meme);
    } catch (CoreException e) {
      add(e);
      return meme;
    }

  }

  @Override
  public Audio add(Audio audio) {
    try {
      requireId("before adding Audio");
      audio.setInstrumentId(getId());
      return SubEntity.add(audioMap, audio);
    } catch (CoreException e) {
      add(e);
      return audio;
    }

  }

  @Override
  public AudioChord add(AudioChord audioChord) {
    try {
      requireId("before adding AudioChord");
      audioChord.setInstrumentId(getId());
      ensureRelations(audioChord);
      return SubEntity.add(audioChordMap, audioChord);
    } catch (CoreException e) {
      add(e);
      return audioChord;
    }

  }

  @Override
  public AudioEvent add(AudioEvent audioEvent) {
    try {
      requireId("before adding AudioEvent");
      audioEvent.setInstrumentId(getId());
      ensureRelations(audioEvent);
      return SubEntity.add(audioEventMap, audioEvent);
    } catch (CoreException e) {
      add(e);
      return audioEvent;
    }

  }

  @Override
  public Instrument consume(Payload payload) throws CoreException {
    super.consume(payload);
    syncSubEntities(payload, memeMap, InstrumentMeme.class);
    syncSubEntities(payload, audioMap, Audio.class);
    syncSubEntities(payload, audioEventMap, AudioEvent.class);
    syncSubEntities(payload, audioChordMap, AudioChord.class);
    return this;
  }

  @Override
  public Collection<SubEntity> getAllSubEntities() {
    Collection<SubEntity> out = Lists.newArrayList();
    out.addAll(getMemes());
    out.addAll(getAudios());
    out.addAll(getAudioChords());
    out.addAll(getAudioEvents());
    return out;
  }

  @Override
  public Collection<InstrumentMeme> getMemes() {
    return memeMap.values();
  }

  @Override
  public Collection<AudioChord> getAudioChords() {
    return audioChordMap.values();
  }

  @Override
  public Collection<AudioEvent> getAudioEvents() {
    return audioEventMap.values();
  }

  @Override
  public Collection<Audio> getAudios() {
    return audioMap.values();
  }

  @Override
  public InstrumentContent getContent() {
    return InstrumentContent.of(this);
  }


  @Override
  public Instrument setAudioChords(Collection<AudioChord> audioChords) {
    audioChordMap.clear();
    for (AudioChord audioChord : audioChords) {
      add(audioChord);
    }
    return this;
  }

  @Override
  public Instrument setAudioEvents(Collection<AudioEvent> audioEvents) {
    audioEventMap.clear();
    for (AudioEvent audioEvent : audioEvents) {
      add(audioEvent);
    }
    return this;
  }

  @Override
  public Instrument setAudios(Collection<Audio> audios) {
    audioMap.clear();
    for (Audio audio : audios) {
      add(audio);
    }
    return this;
  }

  @Override
  public Instrument setContent(String json) {
    InstrumentContent content = gsonProvider.gson().fromJson(json, InstrumentContent.class);
    setMemes(content.getMemes());
    setAudios(content.getAudios());
    setAudioEvents(content.getAudioEvents());
    setAudioChords(content.getAudioChords());
    return this;
  }

  @Override
  public Instrument setMemes(Collection<InstrumentMeme> memes) {
    memeMap.clear();
    for (InstrumentMeme meme : memes) {
      add(meme);
    }
    return this;
  }

  @Override
  public Instrument validateContent() throws CoreException {
    SubEntity.validate(this.getAllSubEntities());
    for (AudioChord audioChord : getAudioChords()) ensureRelations(audioChord);
    for (AudioEvent audioEvent : getAudioEvents()) ensureRelations(audioEvent);
    return this;
  }

  /**
   Ensure that an AudioChord relates to an existing Audio stored in the Program

   @param audioChord to ensure existing relations of
   @throws CoreException if no such Audio exists
   *
  private void ensureRelations(AudioChord audioChord) throws CoreException {
    if (Objects.isNull(audioChord.getInstrumentAudioId())) {
      throw new CoreException(String.format("AudioChord id=%s has null audioId", audioChord.getId()));
    }
    if (!audioMap.containsKey(audioChord.getInstrumentAudioId())) {
      throw new CoreException(String.format("AudioChord id=%s has nonexistent audioId=%s", audioChord.getId(), audioChord.getInstrumentAudioId()));
    }
  }

  /**
   Ensure that an AudioEvent relates to an existing Audio stored in the Program

   @param audioEvent to ensure existing relations of
   @throws CoreException if no such Audio exists
   *
  private void ensureRelations(AudioEvent audioEvent) throws CoreException {
    if (Objects.isNull(audioEvent.getInstrumentAudioId())) {
      throw new CoreException(String.format("AudioEvent id=%s has null audioId", audioEvent.getId()));
    }
    if (!audioMap.containsKey(audioEvent.getInstrumentAudioId())) {
      throw new CoreException(String.format("AudioEvent id=%s has nonexistent audioId=%s", audioEvent.getId(), audioEvent.getInstrumentAudioId()));
    }
  }


 */

/*
  TODO remove this legacy interface declarations

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
   Add an InstrumentMeme (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param meme to add
   @return InstrumentMeme with newly assigned ID
   *
  InstrumentMeme add(InstrumentMeme meme);

  /**
   Add an Audio (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param audio to add
   @return Audio with newly assigned ID
   *
  Audio add(Audio audio);

  /**
   Add an AudioChord (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioChord to add
   @return AudioChord with newly assigned ID
   *
  AudioChord add(AudioChord audioChord);

  /**
   Add an AudioEvent (assigning its ID)
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioEvent to add
   @return AudioEvent with newly assigned ID
   *
  AudioEvent add(AudioEvent audioEvent);

  /**
     Get all entities contained within this entity.
     Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

     @return collection of entities
     *
  Collection<SubEntity> getAllSubEntities();

  /**
   get Audio Chords

   @return Audio Chords
   *
  Collection<AudioChord> getAudioChords();

  /**
   get Audio Events

   @return Audio Events
   *
  Collection<AudioEvent> getAudioEvents();

  /**
   get Audios

   @return Audios
   *
  Collection<Audio> getAudios();

  /**
     Get content of super entity, comprising many sub entities

     @return super entity content
     *
  InstrumentContent getContent();

  /**
   get (computed) Density
   Instrument doesn't actually have density property; it's computed by averaging the density of all its sub entities

   @return Density
   *
  Double getDensity();

  /**
   get Name

   @return Name
   *
  String getName();

  /**
   get Library Id

   @return Library Id
   *
  BigInteger getLibraryId();

  /**
   get Memes

   @return Memes
   *
  Collection<InstrumentMeme> getMemes();

  /**
   get State

   @return State
   *
  InstrumentState getState();

  /**
   get Type

   @return Type
   *
  InstrumentType getType();

  /**
   Get User

   @return User
   *
  BigInteger getUserId();

  /**
   Set all audio chords
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioChords to set
   @return this Instrument (for chaining methods)
   *
  Instrument setAudioChords(Collection<AudioChord> audioChords);

  /**
   Set all audio events
   + If there are any exceptions, store them in the SuperEntity errors

   @param audioEvents to set
   @return this Instrument (for chaining methods)
   *
  Instrument setAudioEvents(Collection<AudioEvent> audioEvents);

  /**
   Set all audio
   + If there are any exceptions, store them in the SuperEntity errors

   @param audios to set
   @return this Instrument (for chaining methods)
   *
  Instrument setAudios(Collection<Audio> audios);

  /**
     Set JSON string content (comprising many sub entities) of super entity

     @param json to set
     @return this super entity (for chaining setters)
     @throws CoreException on bad JSON
     *
  Instrument setContent(String json);

  /**
   Set all content of instrument, cloned of another source instrument, with all new UUID, preserving relationships.
   + If there are any exceptions, store them in the SuperEntity errors

   @param of instrument
   @return this Instrument (for chaining setters)
   *
  Instrument setContentCloned(Instrument of);

  /**
   Set Name

   @param name to set
   @return this Instrument (for chaining Setters)
   *
  Instrument setName(String name);

  /**
   Set Density (this is a no-op to prevent problems with payload deserialization)

   @param density to (not actually) set
   @return this Instrument (for chaining Setters)
   *
  Instrument setDensity(Double density);

  /**
   Set LibraryId

   @param libraryId to set
   @return this Instrument (for chaining Setters)
   *
  Instrument setLibraryId(BigInteger libraryId);

  /**
   Set all memes
   + If there are any exceptions, store them in the SuperEntity errors

   @param memes to set
   @return this Instrument (for chaining Setters)
   *
  Instrument setMemes(Collection<InstrumentMeme> memes);

  /**
   Set state

   @param state to set
   @return this Instrument (for chaining Setters)
   *
  Instrument setState(String state);

  /**
   Set state by enum

   @param state to set
   @return this Instrument (for chaining setters)
   *
  Instrument setStateEnum(InstrumentState state);

  /**
   Set Type

   @param type to set
   @return this Instrument (for chaining setters)
   *
  Instrument setType(String type);

  /**
   Set type enum

   @param type to set
   @return this Instrument (for chaining setters)
   *
  Instrument setTypeEnum(InstrumentType type);

  /**
   Set UserId

   @param userId to set
   @return this Instrument (for chaining Setters)
   *
  Instrument setUserId(BigInteger userId);

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
   Get entity id

   @return entity id
   *
  BigInteger getId();

  /**
   Get parent id

   @return parent id
   *
  BigInteger getParentId();

  /**
   Get updated-at time

   @return updated-at time
   *
  Instant getUpdatedAt();

  /**
   Set createdat time

   @param createdAt time
   @return entity
   *
  Entity setCreatedAt(String createdAt);

  /**
   Set createdat time

   @param createdAt time
   @return entity
   *
  Entity setCreatedAtInstant(Instant createdAt);

  /**
   Set entity id

   @param id to set
   @return entity
   *
  Entity setId(BigInteger id);

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   *
  Entity setUpdatedAt(String updatedAt);

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   *
  Entity setUpdatedAtInstant(Instant updatedAt);

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
   For SuperEntity, that's a BigInteger
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
