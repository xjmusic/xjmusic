// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.link.Link;

import org.jooq.Field;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;

abstract public class Entity {

  /**
   * For use in maps.
   * Should be overridden by extending classes.
   */
  public static final String KEY_ID = "id";
  public static final String KEY_ONE = "entity";
  public static final String KEY_MANY = "entities";


  abstract public void validate() throws BusinessException;

  public abstract Map<Field, Object> intoFieldValueMap();

  /**
   * Get timestamp of string value
   * @param stopAt string
   * @return timestamp
   * @throws Exception on failure
   */
  protected Timestamp buildTimestampOf(String stopAt) throws Exception {
    if (stopAt != null && stopAt.length()>0) {
      return Timestamp.valueOf(stopAt);
    }
    return null;
  }

}
