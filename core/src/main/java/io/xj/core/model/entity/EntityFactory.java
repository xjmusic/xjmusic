//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

@FunctionalInterface
public interface EntityFactory<E extends Entity> {
  E newInstance();
}
