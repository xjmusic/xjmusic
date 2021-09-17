package io.xj.lib;

import java.util.UUID;

public class Superwidget {
  private UUID id;
  private String name;

  public Superwidget setId(UUID id) {
    this.id = id;
    return this;
  }

  public UUID getId() {
    return id;
  }

  public Superwidget setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }
}
