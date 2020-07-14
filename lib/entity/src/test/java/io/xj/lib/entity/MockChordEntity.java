// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import io.xj.lib.entity.common.ChordEntity;

import java.util.UUID;

public class MockChordEntity extends ChordEntity {
  private UUID id;
  private String name;
  private UUID mockEntityId;

  public static MockChordEntity create() {
    return new MockChordEntity();
  }

  public static MockChordEntity create(double position, String name) {
    return (MockChordEntity) new MockChordEntity()
      .setPosition(position)
      .setName(name);
  }

  public UUID getId() {
    return id;
  }

  public MockChordEntity setId(UUID id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public MockChordEntity setName(String name) {
    this.name = name;
    return this;
  }

  public UUID getMockEntityId() {
    return mockEntityId;
  }

  public MockChordEntity setMockEntityId(UUID mockEntityId) {
    this.mockEntityId = mockEntityId;
    return this;
  }
}
