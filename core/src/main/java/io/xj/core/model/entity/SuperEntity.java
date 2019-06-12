//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import io.xj.core.exception.CoreException;

import java.util.Collection;

/**
 Subclass shared by Segment, Pattern, and Audio, being parent entities to many child entities
 <p>
 First these were implemented:
 + [#166132897] Segment model handles all of its own entities
 + [#166273140] Segment Child Entities are identified and related by UUID (not id)
 <p>
 Then these followers influenced us to extract this subclass:
 + [#166690830] Pattern model handles all of its own entities
 + [#166708597] Audio model handles all of its own entities
 */
public interface SuperEntity extends Entity {
  /**
   Get content of super entity, comprising many sub entities

   @return super entity content
   */
  SuperEntityContent getContent();

  /**
   Get all entities

   @return collection of entities
   */
  Collection<SubEntity> getAllSubEntities();

  /**
   Set JSON string content (comprising many sub entities) of super entity

   @param json to set
   @return this super entity (for chaining setters)
   @throws CoreException on bad JSON
   */
  SuperEntity setContent(String json) throws CoreException;

  /**
   Validate that all entities have an id,
   that none of the entities provided share an id, and that relation ids are OK

   @return this super entity (for chaining setters)
   @throws CoreException if invalid attributes, or child entities have duplicate ids or bad relations are detected
   */
  SuperEntity validateContent() throws CoreException;
}
