// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.inject.spi.Message;
import io.xj.lib.entity.common.MessageEntity;

import java.util.UUID;

public class MockMessageEntity extends MessageEntity {
  private UUID id;
  private UUID mockEntityId;

  public static MockMessageEntity create() {
    return new MockMessageEntity();
  }

  public static MockMessageEntity create(MessageType type, String body) {
    return (MockMessageEntity) new MockMessageEntity()
      .setTypeEnum(type)
      .setBody(body);
  }

  public UUID getId() {
    return id;
  }

  public MockMessageEntity setId(UUID id) {
    this.id = id;
    return this;
  }

  public UUID getMockEntityId() {
    return mockEntityId;
  }

  public MockMessageEntity setMockEntityId(UUID mockEntityId) {
    this.mockEntityId = mockEntityId;
    return this;
  }
}
