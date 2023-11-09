// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Widget {
  UUID id;
  String name;
  UUID superwidgetId;
  WidgetState state;
  double position;
  LocalDateTime createdAt;

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

  public LocalDateTime getCreatedAt() {
    return this.createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

}
