// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import java.util.UUID;

/**
 Template for testing REST API payload mock entities
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class TestTemplate {

  /**
   Create a new MockEntity with the given name

   @param name of mock entity
   @return new mock entity
   */
  protected MockEntity createMockEntity(String name) {
    MockEntity e = createMockEntity(UUID.randomUUID());
    e.setName(name);
    return e;
  }

  /**
   Create a new MockEntity with the given id and name

   @param id   of mock entity
   @param name of mock entity
   @return new mock entity
   */
  protected MockEntity createMockEntity(UUID id, String name) {
    MockEntity e = createMockEntity(id);
    e.setName(name);
    return e;
  }

  /**
   Create a new MockEntity with the given name

   @param id of mock entity
   @return new mock entity
   */
  protected MockEntity createMockEntity(UUID id) {
    MockEntity e = new MockEntity();
    e.setId(id);
    return e;
  }
}
