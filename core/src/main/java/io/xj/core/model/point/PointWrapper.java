// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.point;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PointWrapper {
  private Point point;

  public Point getPoint() {
    return point;
  }

  public PointWrapper setPoint(Point point) {
    this.point = point;
    return this;
  }
}
