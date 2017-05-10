// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.meme;

import io.outright.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;

import javax.annotation.Nullable;
import java.util.Map;

public class GenericMeme extends Meme {

  @Override
  public GenericMeme setName(String name) {
    this.name = name;
    return this;
  }

  @Nullable
  @Override
  public Entity setFromRecord(Record record) {
    return null;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    return null;
  }
}
