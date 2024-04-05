// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.entity;

import io.xj.hub.util.Widget;

import java.util.UUID;

/**
 * Template for testing REST API payload mock entities
 * <p>
 * Created by Charney Kaye on 2020/03/09
 */
public class TestTemplate {

  /**
   * Create a new Widget with the given id and name
   *
   * @param superwidgetId of mock entity
   * @param name          of mock entity
   * @return new mock entity
   */
  public static Widget createWidget(UUID superwidgetId, String name) {
    return new Widget()
      .setId(UUID.randomUUID())
      .setSuperwidgetId(superwidgetId)
      .setName(name);
  }

  /**
   * Create a new Widget with the given name
   *
   * @param name of mock entity
   * @return new mock entity
   */
  public static Widget createWidget(String name) {
    return new Widget()
      .setId(UUID.randomUUID())
      .setName(name);
  }
}
