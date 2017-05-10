// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.point;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PointWrapper extends EntityWrapper {

  // Point
  private Point point;

  public Point getPoint() {
    return point;
  }

  public PointWrapper setPoint(Point point) {
    this.point = point;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Point validate() throws BusinessException {
    if (this.point == null) {
      throw new BusinessException("Point is required.");
    }
    this.point.validate();
    return this.point;
  }

}
