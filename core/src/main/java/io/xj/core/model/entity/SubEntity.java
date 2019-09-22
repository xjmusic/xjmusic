//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import com.google.common.collect.Lists;
import io.xj.core.exception.CoreException;
import io.xj.core.util.Text;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 Subclass shared by Segment, Pattern, and Audio child entities
 <p>
 First these were implemented:
 + [#166132897] Segment model handles all of its own entities
 + [#166273140] Segment Child Entities are identified and related by UUID (not id)
 <p>
 Then these followers influenced us to extract this subclass:
 + [#166690830] Pattern model handles all of its own entities
 + [#166708597] Audio model handles all of its own entities
 */
public interface SubEntity<N extends SubEntity> extends ResourceEntity {

  /**
   Add an entity to its internal map
   Assign the next unique Id to an entity,
   unless that entity already as an Id, in which case, ensure that the next unique one will be higher.
   Also ensure that id does not already exist in its map

   @param entity to assign next unique id
   */
  static <E extends SubEntity> E add(Map<UUID, E> entities, E entity) throws CoreException {
    if (Objects.isNull(entity.getId())) {
      entity.setId(UUID.randomUUID());
    } else {
      if (entities.containsKey(entity.getId())) {
        throw new CoreException(String.format("%s id=%s already exists", Text.getSimpleName(entity), entity.getId()));
      }
    }
    entity.validate();
    entities.put(entity.getId(), entity);
    return entity;
  }

  /**
   Validate uniqueness within a collection of sub entities

   @param allEntities to validate uniqueness within
   @throws CoreException when invalid
   */
  static <E extends SubEntity> void validate(Collection<E> allEntities) throws CoreException {
    Collection<UUID> ids = Lists.newArrayList();
    for (SubEntity entity : allEntities) {
      if (Objects.isNull(entity.getId())) {
        throw new CoreException(String.format("Contains a %s with null id", Text.getSimpleName(entity)));
      }
      if (ids.contains(entity.getId())) {
        throw new CoreException(String.format("Contains %s with duplicate id=%s", Text.getSimpleName(entity), entity.getId()));
      }
      try {
        entity.validate();
      } catch (CoreException e) {
        throw new CoreException(String.format("%s with id=%s is invalid", Text.getSimpleName(entity), entity.getId()), e);
      }
      ids.add(entity.getId());
    }
  }

  /**
   Instantiate a SubEntity by class

   @param subClass to instantiate
   @param <Sub>    class
   @return new subentity instance
   @throws CoreException on failure
   */
  static <Sub extends SubEntity> Sub newInstance(Class<Sub> subClass) throws CoreException {
    try {
      return subClass.getConstructor().newInstance();
    } catch (Exception e) {
      throw new CoreException(String.format("Cannot construct new instance from SubEntity class %s", subClass), e);
    }
  }

  /**
   Get child entity id

   @return entity id
   */
  UUID getId();

  /**
   Set child entity id

   @return this segment entity (for chaining setters)
   */
  SubEntity setId(UUID id);

  /**
   Get parent id

   @return parent id
   */
  BigInteger getParentId();

}
