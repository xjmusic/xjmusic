// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

public class MockEntity {
  private String id;
  private String name;
  private String mockEntityId;

  public String getId() {
    return id;
  }

  public MockEntity setId(String id) {
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

  public String getMockEntityId() {
    return mockEntityId;
  }

  public MockEntity setMockEntityId(String mockEntityId) {
    this.mockEntityId = mockEntityId;
    return this;
  }
}
