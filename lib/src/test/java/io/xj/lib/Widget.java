// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib;

import java.util.UUID;

public class Widget {
  private UUID id;
  private String name;
  private UUID superwidgetId;
  private WidgetState state;
  private double position;

  public UUID getId() {
    return id;
  }

  public Widget setId(UUID id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Widget setName(String name) {
    this.name = name;
    return this;
  }

  public UUID getSuperwidgetId() {
    return superwidgetId;
  }

  public Widget setSuperwidgetId(UUID superwidgetId) {
    this.superwidgetId = superwidgetId;
    return this;
  }

  public WidgetState getState() {
    return state;
  }

  public Widget setState(WidgetState state) {
    this.state = state;
    return this;
  }

  public double getPosition() {
    return position;
  }

  public Widget setPosition(double position) {
    this.position = position;
    return this;
  }
}
