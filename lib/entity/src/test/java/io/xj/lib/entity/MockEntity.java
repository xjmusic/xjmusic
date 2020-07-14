// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import java.util.UUID;

public class MockEntity extends Entity {
  private UUID id;
  private String name;
  private UUID mockEntityId;
  private UUID parentId;

  public static MockEntity create() {
    return new MockEntity().setId(UUID.randomUUID());
  }

  public UUID getId() {
    return id;
  }

  public MockEntity setId(UUID id) {
    this.id = id;
    return this;
  }

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

  public UUID getParentId() {
    return parentId;
  }

  public void setParentId(UUID parentId) {
    this.parentId = parentId;
  }
}
