// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.work;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.JSONObjectEntity;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Map;

public class Work extends JSONObjectEntity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "work";
  public static final String KEY_MANY = "works";

  /**
   Fields
   */
  private ULong targetId;
  private WorkState state;
  private WorkType type;

  public ULong getTargetId() {
    return targetId;
  }

  public Work setTargetId(BigInteger value) {
    targetId = ULong.valueOf(value);
    return this;
  }

  public Work setTargetId(ULong value) {
    targetId = value;
    return this;
  }

  public WorkState getState() {
    return state;
  }

  public Work setState(WorkState value) {
    state = value;
    return this;
  }

  public WorkType getType() {
    return type;
  }

  public Work setType(WorkType value) {
    type = value;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == targetId) {
      throw new BusinessException("Target ID is required.");
    }
    if (null == type || type.toString().isEmpty()) {
      throw new BusinessException("Type is required.");
    }
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject();
    obj.put("id", id);
    obj.put("targetId", targetId);
    obj.put("state", state);
    obj.put("type", type);
    return obj;
  }

  @Override
  public Work setFromRecord(Record record) throws BusinessException {
    return null;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    return Maps.newHashMap();
  }

  @Override
  public String toString() {
    return "Work{...}";
  }

}
