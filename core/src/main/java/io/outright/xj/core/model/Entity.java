// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.link.Link;

import org.jooq.Field;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

abstract public class Entity {

  /**
   * For use in maps.
   */
  public static final String KEY_ID = "id";

  abstract public void validate() throws BusinessException;

  public abstract Map<Field, Object> intoFieldValueMap();

  /**
   * For use in maps.
   * Should be overridden by extending classes.
   */
  public static final String KEY_ONE = "entity";
  public static final String KEY_MANY = "entities";

}
