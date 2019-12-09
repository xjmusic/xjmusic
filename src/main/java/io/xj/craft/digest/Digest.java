// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;
import io.xj.core.entity.Entity;
import io.xj.core.payload.PayloadObject;
import io.xj.core.util.TremendouslyRandom;
import io.xj.core.util.Value;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public interface Digest {
  String KEY_CHORD_NAME = "chordName";
  String KEY_CHORD_POSITION = "chordPosition";
  String RESOURCE_TYPE = "digests";

  /**
   Get the most popular entry in a histogram

   @param histogram to get most popular entry of
   @param <N>       type of number
   @return most popular entry
   */
  static <N extends Number> N mostPopular(Multiset<N> histogram, N defaultIfZero) {
    N result = null;
    Integer popularity = null;
    for (Multiset.Entry<N> entry : histogram.entrySet()) {
      if (Objects.isNull(popularity) || entry.getCount() > popularity) {
        popularity = entry.getCount();
        result = entry.getElement();
      }
    }
    return Objects.nonNull(result) ? result : defaultIfZero;
  }

  /**
   Selects an integer by random of a histogram; each entry in the histogram is added to the lottery N times, where N is the number of occurrences of the entry in the histogram, such that an entry occurring more often in the histogram is more likely to be selected in the lottery.

   @param <N> type of number
   @return randomly selected integer
   */
  static <N extends Number> N lottery(Multiset<N> histogram, N defaultIfZero) {
    List<N> lottery = Lists.newArrayList();
    lottery.addAll(histogram);
    N winner = lottery.get(TremendouslyRandom.zeroToLimit(lottery.size()));
    return Objects.equals(0, winner) ? defaultIfZero : winner;
  }

  /**
   Selects the max value of a stats object

   @param stats         to select max of
   @param defaultIfZero if max is zero or exception is caught
   @return max value
   */
  static double max(StatsAccumulator stats, double defaultIfZero) {
    try {
      double result = stats.max();
      return Objects.equals(0.0, result) ? defaultIfZero : result;
    } catch (Exception ignored) {
      return defaultIfZero;
    }
  }

  /**
   Get histogram elements all divided by a common divisor, with a default value if none found

   @param histogram     to get elements of, divided
   @param divisor       to divide elements by
   @param defaultIfNone only member of set if none other are found.
   @return resulting elements divided, else set containing default divided by divisor
   */
  static Set<Integer> elementsDividedBy(Multiset<Integer> histogram, double divisor, int defaultIfNone) {
    Set<Integer> result = Value.dividedBy(divisor, histogram.elementSet());
    if (result.isEmpty()) return ImmutableSet.of((int) Math.floor(defaultIfNone / divisor));
    return result;
  }

  /**
   * Get an entity from this digest that can be
   * @return payload object of digest
   */
  PayloadObject getPayloadObject();


  /*

    TODO remove all of the the following that we don't need

  /**
   Add an exception to the SuperEntity errors

   @param exception to add
   *
  void add(CoreException exception);

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
   Validate data.

   @throws CoreException if invalid.
   *
  void validate() throws CoreException;


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
   Get all entities contained within this entity.
   Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

   @return collection of entities
   *
  Collection<Entity> getAllEntities();

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

   @param from source Entity
   *
  void setAllResourceAttributes(Entity from);

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
*/
}
