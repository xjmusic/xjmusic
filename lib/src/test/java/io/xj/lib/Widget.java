package io.xj.lib;

import java.util.UUID;

public class Widget {
  private UUID id;
  private String name;
  private UUID superwidgetId;
  private WidgetState state;
  private double position;

  public Widget setId(UUID id) {
    this.id = id;
    return this;
  }

  public UUID getId() {
    return id;
  }

  public Widget setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public Widget setSuperwidgetId(UUID superwidgetId) {
    this.superwidgetId = superwidgetId;
    return this;
  }

  public UUID getSuperwidgetId() {
    return superwidgetId;
  }

  public Widget setState(WidgetState state) {
    this.state = state;
    return this;
  }

  public WidgetState getState() {
    return state;
  }

  public Widget setPosition(double position) {
    this.position = position;
    return this;
  }

  public double getPosition() {
    return position;
  }
}
