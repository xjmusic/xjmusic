// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

import java.util.UUID;

public class Superwidget {
  UUID id;
  String name;

  public UUID getId() {
    return id;
  }

  public Superwidget setId(UUID id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public Superwidget setName(String name) {
    this.name = name;
    return this;
  }
}
