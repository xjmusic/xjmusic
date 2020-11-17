// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import io.xj.Program;

import java.util.UUID;

/**
 Template for testing REST API payload mock entities
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class TestTemplate {

  /**
   Create a new Program with the given id and name

   @param libraryId   of mock entity
   @param name of mock entity
   @return new mock entity
   */
  public static Program createProgram(String libraryId, String name) {
    return Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(libraryId)
      .setName(name)
      .build();
  }

  /**
   Create a new Program with the given name

   @param id of mock entity
   @return new mock entity
   */
  public static Program createProgram(String id) {
    return Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
  }
}
