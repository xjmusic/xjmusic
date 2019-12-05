// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.util.Text;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 [#166743281] Chain handles all of its own binding + config entities
 */
public class Chain extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .add("name")
    .add("state")
    .add("type")
    .add("startAt")
    .add("stopAt")
    .add("embedKey")
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .add(Account.class)
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .add(ChainBinding.class)
    .add(ChainConfig.class)
    .build();

  private UUID accountId;
  private String name;
  private ChainState state;
  private ChainType type;
  private Instant startAt;
  private Exception startAtException;
  private Instant stopAt;
  private Exception stopAtException;
  @Nullable
  private String embedKey;
  private Exception stateException;
  private Exception typeException;

  /**
   Get a new Chain

   @return new Chain
   */
  public static Chain create() {
    return new Chain().setId(UUID.randomUUID());
  }

  /**
   Get a new Chain

   @param state of Chain
   @return new Chain
   */
  public static Chain create(ChainState state) {
    return create()
      .setStateEnum(state);
  }

  /**
   Get a new Chain

   @param account  of Chain
   @param name     of Chain
   @param type     of Chain
   @param state    of Chain
   @param startAt  of Chain
   @param stopAt   of Chain
   @param embedKey of Chain
   @return new Chain
   */
  public static Chain create(Account account, String name, ChainType type, ChainState state, Instant startAt, @Nullable Instant stopAt, @Nullable String embedKey) {
    return create(state)
      .setAccountId(account.getId())
      .setTypeEnum(type)
      .setName(name)
      .setStartAtInstant(startAt)
      .setStopAtInstant(stopAt)
      .setEmbedKey(embedKey);
  }

  /**
   * Format an embed key
   * @param embedKey to format
   * @return formatted embed key
   */
  public static String formatEmbedKey(String embedKey) {
    return Text.toLowerScored(embedKey);
  }

  /**
   get AccountId

   @return AccountId
   */
  public UUID getAccountId() {
    return accountId;
  }

  /**
   get EmbedKey

   @return EmbedKey
   */
  @Nullable
  public String getEmbedKey() {
    return embedKey;
  }

  /**
   get Name

   @return Name
   */
  public String getName() {
    return name;
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
   get StartAt

   @return StartAt
   */
  public Instant getStartAt() {
    return startAt;
  }

  /**
   get State

   @return State
   */
  public ChainState getState() {
    return state;
  }

  /**
   get StopAt

   @return StopAt
   */
  public Instant getStopAt() {
    return stopAt;
  }

  /**
   get Type

   @return Type
   */
  public ChainType getType() {
    return type;
  }

  /**
   Set account id

   @param accountId to set
   @return this Chain (for chaining setters)
   */
  public Chain setAccountId(UUID accountId) {
    this.accountId = accountId;
    return this;
  }

  /**
   set EmbedKey

   @param embedKey to set
   @return this Chain (for chaining setters)
   */
  public Chain setEmbedKey(String embedKey) {
    String key = formatEmbedKey(embedKey);
    if (key.isEmpty()) {
      this.embedKey = null;
    } else {
      this.embedKey = key;
    }
    return this;
  }

  /**
   set Id

   @param id to set
   @return this Chain (for chaining setters)
   */
  public Chain setId(UUID id) {
    super.setId(id);
    return this;
  }

  /**
   set Name

   @param name to set
   @return this Chain (for chaining setters)
   */
  public Chain setName(String name) {
    this.name = name;
    return this;
  }

  /**
   set StartAt

   @param value to set
   @return this Chain (for chaining setters)
   */
  public Chain setStartAt(String value) {
    if (Objects.equals("now", Text.toLowerSlug(value))) {
      startAt = Instant.now();
    } else try {
      startAt = Instant.parse(value);
    } catch (Exception e) {
      startAtException = e;
    }
    return this;
  }

  /**
   set StartAtInstant

   @param startAtInstant to set
   @return this Chain (for chaining setters)
   */
  public Chain setStartAtInstant(Instant startAtInstant) {
    startAt = startAtInstant;
    return this;
  }

  /**
   set State

   @param state to set
   @return this Chain (for chaining setters)
   */
  public Chain setState(String state) {
    try {
      this.state = ChainState.validate(state);
    } catch (CoreException e) {
      stateException = e;
    }
    return this;
  }

  /**
   set StateEnum

   @param state to set
   @return this Chain (for chaining setters)
   */
  public Chain setStateEnum(ChainState state) {
    this.state = state;
    return this;
  }

  /**
   set Type

   @param type to set
   @return this Chain (for chaining setters)
   */
  public Chain setType(String type) {
    try {
      this.type = ChainType.validate(type);
    } catch (CoreException e) {
      typeException = e;
    }
    return this;
  }

  /**
   set TypeEnum

   @param type to set
   @return this Chain (for chaining setters)
   */
  public Chain setTypeEnum(ChainType type) {
    this.type = type;
    return this;
  }

  /**
   set StopAt

   @param value to set
   @return this Chain (for chaining setters)
   */
  public Chain setStopAt(String value) {
    if (Objects.nonNull(value) && !value.isEmpty())
      try {
        stopAt = Instant.parse(value);
      } catch (Exception e) {
        stopAtException = e;
      }
    return this;
  }

  /**
   set StopAtInstant

   @param stopAtInstant to set
   @return this Chain (for chaining setters)
   */
  public Chain setStopAtInstant(Instant stopAtInstant) {
    stopAt = stopAtInstant;
    return this;
  }

/*
  TODO address chain cloning
  public Chain setContentCloned(Chain of) {
    setConfigs(of.getConfigs());
    setBindings(of.getBindings());
    return this;
  }
*/

  @Override
  public String toString() {
    return Text.toKeyValueString("Chain", ImmutableMap.<String, String>builder()
      .put("id", String.valueOf(id))
      .put("accountId", String.valueOf(accountId))
      .put("name", Text.toSingleQuoted(name))
      .put("state", Text.toSingleQuoted(String.valueOf(state)))
      .put("type", Text.toSingleQuoted(String.valueOf(type)))
      .put("startAt", String.valueOf(startAt))
      .put("stopAt", String.valueOf(stopAt))
      .put("createdAt", String.valueOf(createdAt))
      .put("updatedAt", String.valueOf(updatedAt))
      .build());
  }

  @Override
  public void validate() throws CoreException {
    require(accountId, "Account ID");
    require(name, "Name");

    requireNo(typeException, "Type");
    if (isEmpty(type)) type = ChainType.Preview;

    requireNo(stateException, "State");
    if (isEmpty(state)) state = ChainState.Draft;

    requireNo(startAtException, "Start-at");
    require(startAt, "Start-at time");

    requireNo(stopAtException, "Stop-at");
    if (ChainType.Production != type)
      require(stopAt, "Stop-at time (for non-production chain)");

/*
TODO address blocking transition of chain unless bound to library
    if (ChainState.Ready == state || ChainState.Fabricate == state)
      if (bindingMap.isEmpty())
        throw new CoreException(String.format("Chain must be bound to %s in order to enter Fabricate state.", "at least one Library, Sequence, or Instrument"));
*/
  }












































/*
TODO remove this legacy chain interface declarations

  @Override
  public ChainBinding add(ChainBinding chainBinding) {
    try {
      requireId("before adding Chain Binding");
      chainBinding.setChainId(getId());
      return SubEntity.add(bindingMap, chainBinding);
    } catch (CoreException e) {
      add(e);
      return chainBinding;
    }
  }

  @Override
  public ChainConfig add(ChainConfig chainConfig) {
    try {
      requireId("before adding Chain Config");
      chainConfig.setChainId(getId());
      return SubEntity.add(configMap, chainConfig);
    } catch (CoreException e) {
      add(e);
      return chainConfig;
    }
  }

  @Override
  public Chain consume(Payload payload) throws CoreException {
    super.consume(payload);
    syncSubEntities(payload, configMap, ChainConfig.class);
    syncSubEntities(payload, bindingMap, ChainBinding.class);
    return this;
  }

  @Override
  public Collection<SubEntity> getAllSubEntities() {
    Collection<SubEntity> out = Lists.newArrayList();
    out.addAll(getConfigs());
    out.addAll(getBindings());
    return out;
  }

  @Override
  public ChainBinding getBinding(UUID id) throws CoreException {
    if (!bindingMap.containsKey(id))
      throw new CoreException(String.format("Found no Binding id=%s", id));
    return bindingMap.get(id);
  }

  @Override
  public Collection<ChainBinding> getBindings() {
    return bindingMap.values();
  }

  @Override
  public ChainConfig getConfig(UUID id) throws CoreException {
    if (!configMap.containsKey(id))
      throw new CoreException(String.format("Found no Config id=%s", id));
    return configMap.get(id);
  }

  @Override
  public Collection<ChainConfig> getConfigs() {
    return configMap.values();
  }

  @Override
  public ChainContent getContent() {
    return ChainContent.of(this);
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
   Add a ChainBinding
   + If there are any exceptions, store them in the SuperEntity errors

   @param chainBinding to add
   @return newly added ChainBinding with generated id
   *
  ChainBinding add(ChainBinding chainBinding);

  /**
   Add a ChainConfig
   + If there are any exceptions, store them in the SuperEntity errors

   @param chainConfig to add
   @return newly added ChainConfig with generated id
   *
  ChainConfig add(ChainConfig chainConfig);

  /**
   Get Account Id

   @return account id
   *
  BigInteger getAccountId();

  /**
     Get all entities contained within this entity.
     Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

     @return collection of entities
     *
  Collection<SubEntity> getAllSubEntities();

  /**
   Get a ChainBinding by id

   @param id of chain binding
   @return chain binding
   @throws CoreException if none is found with specified id
   *
  ChainBinding getBinding(UUID id) throws CoreException;

  /**
   Get all Chain Bindings

   @return Chain Bindings
   *
  Collection<ChainBinding> getBindings();
  
  /**
   Get a ChainConfig by id

   @param id of chain config
   @return chain config
   @throws CoreException if none is found with specified id
   *
  ChainConfig getConfig(UUID id) throws CoreException;

  /**
   Get all Chain Configs

   @return Chain Configs
   *
  Collection<ChainConfig> getConfigs();

  /**
   Get embed key  (optional value, may return null)

   @return embed key, or null if none is set
   *
  @Nullable
  String getEmbedKey();

  /**
   Get name

   @return name
   *
  String getName();

  /**
   Get start-at instant

   @return start-at instant
   *
  Instant getStartAt();

  /**
   Get state

   @return state
   *
  ChainState getState();

  /**
   Get stop-at instant

   @return stop-at instant
   *
  Instant getStopAt();

  /**
   Get type

   @return type
   *
  ChainType getType();

  /**
   Set Account Id

   @param accountId to set
   @return this Chain (for chaining setters)
   *
  Chain setAccountId(BigInteger accountId);

  /**
   Set all Bindings
   + If there are any exceptions, store them in the SuperEntity errors

   @param bindings to set
   @return this Chain (for chaining setters)
   *
  Chain setBindings(Collection<ChainBinding> bindings);

  /**
   Set all Configs
   + If there are any exceptions, store them in the SuperEntity errors

   @param configs to set
   @return this Chain (for chaining setters)
   *
  Chain setConfigs(Collection<ChainConfig> configs);

  /**
     Set JSON string content (comprising many sub entities) of super entity

     @param json to set
     @return this super entity (for chaining setters)
     @throws CoreException on bad JSON
     *
  Chain setContent(String json) throws CoreException;

  /**
   Set all content of chain, cloned of another source chain, with all new UUID, preserving relationships.

   @param of chain
   @return this Chain (for chaining methods)
   *
  Chain setContentCloned(Chain of) throws CoreException;

  /**
   Set Embed Key

   @param embedKey to set
   @return this Chain (for chaining setters)
   *
  Chain setEmbedKey(String embedKey);

  /**
     Set entity id

     @param id to set
     @return entity
     *
  Chain setId(BigInteger id);

  /**
   Set Name

   @param name to set
   @return this Chain (for chaining setters)
   *
  Chain setName(String name);

  /**
   Set Chain start-at time, or set it to "now" for the current time.

   @param value "now" for current time, or ISO-8601 timestamp
   @return this Chain (for chaining setters)
   *
  Chain setStartAt(String value);

  /**
   Set chain start-at at instant

   @param startAtInstant to set
   @return this Chain (for chaining setters)
   *
  Chain setStartAtInstant(Instant startAtInstant);

  /**
   Set state of chain

   @param state to set
   @return this Chain (for chaining setters)
   *
  Chain setState(String state);

  /**
   Set state of chain, by enum

   @param state enum to set
   @return this Chain (for chaining setters)
   *
  Chain setStateEnum(ChainState state);

  /**
   Set state of chain

   @param type to set
   @return this Chain (for chaining setters)
   *
  Chain setType(String type);

  /**
   Set state of chain, by enum

   @param type enum to set
   @return this Chain (for chaining setters)
   *
  Chain setTypeEnum(ChainType type);

  /**
   Set Stop-at value

   @param value to set
   @return this Chain (for chaining setters)
   *
  Chain setStopAt(String value);

  /**
   Set Stop-at instant

   @param stopAtInstant to set
   @return this Chain (for chaining setters)
   *
  Chain setStopAtInstant(Instant stopAtInstant);

  /**
   Get content of super entity, comprising many sub entities

   @return super entity content
   *
  SuperEntityContent getContent();

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
