//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.payload.PayloadObject;

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
public abstract class SubEntityImpl extends ResourceEntityImpl implements SubEntity {
  private static final ImmutableList<String> SUB_ENTITY_PAYLOAD_ATTRIBUTE_NAMES = ImmutableList.of();
  protected UUID id;

  @Override
  public SubEntity consume(PayloadObject payloadObject) throws CoreException {
    super.consume(payloadObject);

    if (Objects.nonNull(payloadObject.getId()))
      setId(UUID.fromString(payloadObject.getId()));

    return this;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return SUB_ENTITY_PAYLOAD_ATTRIBUTE_NAMES;
  }

  @Override
  public String getResourceId() {
    return String.valueOf(id);
  }

  @Override
  public SubEntity setId(UUID id) {
    this.id = id;
    return this;
  }

}
