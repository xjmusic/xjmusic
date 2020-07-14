// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import io.xj.lib.entity.Entity;

import java.util.UUID;

public class MockEntity extends Entity {
  private String name;
  private UUID mockEntityId;

  public String getName() {
    return name;
  }

  public MockEntity setName(String name) {
    this.name = name;
    return this;
  }

  public UUID getMockEntityId() {
    return mockEntityId;
  }

  public MockEntity setMockEntityId(UUID mockEntityId) {
    this.mockEntityId = mockEntityId;
    return this;
  }

  @Override
  public MockEntity setId(UUID id) {
    super.setId(id);
    return this;
  }
}
