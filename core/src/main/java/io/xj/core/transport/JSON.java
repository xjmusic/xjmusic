// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.xj.core.config.Exposure;
import io.xj.core.model.entity.Entity;
import io.xj.core.transport.impl.TimestampSerializer;
import io.xj.core.util.CamelCasify;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.Objects;

public interface JSON {

  /**
   JSONArray from a SQL ResultSet

   @param rs ResultSet to build JSON of
   @return JSON array
   @throws SQLException  on db failure
   @throws JSONException on failure to construct output
   */
  static JSONArray arrayOf(ResultSet rs) throws SQLException {
    JSONArray result = new JSONArray();
    ResultSetMetaData data = rs.getMetaData();
    int maxCol = data.getColumnCount() + 1;

    // Cache JSON key names for SQL column names SQL columns with null values here will not be exposed as JSON
    String[] key = new String[maxCol];
    for (int i = 1; i < maxCol; i++) {
      key[i] = CamelCasify.ifNeeded(data.getColumnLabel(i));
    }

    while (rs.next()) {
      result.put(objectOf(rs, maxCol, key, data));
    }

    return result;
  }

  /**
   JSONObject from one row of a SQL ResultSet

   @param rs     ResultSet to build JSON of
   @param maxCol maximum columns
   @param key    of column name
   @param data   metadata of result set
   @return JSONObject from row of result set
   @throws SQLException on failure
   */
  static JSONObject objectOf(ResultSet rs, int maxCol, String[] key, ResultSetMetaData data) throws SQLException {
    JSONObject obj = new JSONObject();
    for (int i = 1; i < maxCol; i++) {
      if (null != key[i]) {
        switch (data.getColumnType(i)) {
          case Types.ARRAY:
            obj.put(key[i], rs.getArray(i));
            break;
          case Types.BIGINT:
            obj.put(key[i], rs.getInt(i));
            break;
          case Types.BOOLEAN:
            obj.put(key[i], rs.getBoolean(i));
            break;
          case Types.BLOB:
            obj.put(key[i], rs.getBlob(i));
            break;
          case Types.DOUBLE:
            obj.put(key[i], rs.getDouble(i));
            break;
          case Types.FLOAT:
            obj.put(key[i], rs.getFloat(i));
            break;
          case Types.INTEGER:
            obj.put(key[i], rs.getInt(i));
            break;
          case Types.NVARCHAR:
            obj.put(key[i], rs.getNString(i));
            break;
          case Types.VARCHAR:
            obj.put(key[i], rs.getString(i));
            break;
          case Types.TINYINT:
          case Types.SMALLINT:
            obj.put(key[i], rs.getInt(i));
            break;
          case Types.DATE:
            obj.put(key[i], rs.getDate(i));
            break;
          case Types.TIMESTAMP:
            obj.put(key[i], rs.getTimestamp(i));
            break;
          default:
            obj.put(key[i], rs.getObject(i));
        }
      }
    }
    return obj;
  }

  /**
   JSONArray from a single jOOQ Result

   @param items to put into array
   @return array of a single result
   */
  static <J extends Entity> JSONArray arrayOf(Iterable<J> items) {
    JSONArray out = new JSONArray();
    for (J item : items) {
      out.put(objectFrom(item));
    }
    return out;
  }

  /**
   JSONObject of many keys mapped to JSONObject

   @param data map to build object of arrays from
   @return JSONObject
   */
  static JSONObject wrap(Map<String, JSONArray> data) {
    JSONObject out = new JSONObject();
    data.forEach(out::put);
    return out;
  }

  /**
   JSONObject wrapping an internal object put at a root node

   @param rootName root node name
   @param data     to put in root node
   @return JSON object
   */
  static JSONObject wrap(String rootName, JSONObject data) {
    JSONObject result = new JSONObject();
    result.put(rootName, data);
    return result;
  }

  /**
   JSONObject wrapping an internal array put at a root node

   @param rootName root node name
   @param data     to put in root node
   @return JSON object
   */
  static JSONObject wrap(String rootName, JSONArray data) {
    JSONObject result = new JSONObject();
    result.put(rootName, data);
    return result;
  }

  /**
   JSONObject wrapping an error

   @param message to wrap as error
   @return JSON object
   */
  static JSONObject wrapError(String message) {
    JSONObject error = new JSONObject();
    error.put(Exposure.KEY_ERROR_DETAIL, message);

    JSONArray errorsArr = new JSONArray();
    errorsArr.put(error);

    return wrap(Exposure.KEY_ERRORS, errorsArr);
  }

  /**
   JSONObject from a POJO Entity
   <p>
   FUTURE: Is this inefficient, to parse with Jackson then re-parse with JSONObject?

   @param obj to create JSONObject from
   @return JSONObject
   */
  @Nullable
  static JSONObject objectFrom(@Nullable Object obj) {
    if (Objects.isNull(obj)) return null;
    return new JSONObject(gson().toJson(obj));
  }

  /**
   Get a Gson instance

   @return Gson
   */
  static Gson gson() {
    GsonBuilder gson = new GsonBuilder();
    gson.registerTypeAdapter(Timestamp.class, new TimestampSerializer());
    gson.disableInnerClassSerialization();
    return gson.create();
  }

  /**
   Ensure JSONObject has JSONArray at specified key

   @param obj  in which to ensure JSONArray is present at attribute
   @param attr at which to ensure a JSONArray is present in object
   */
  static void ensureArrayAt(JSONObject obj, String attr) {
    if (!obj.has(attr)) {
      obj.put(attr, new JSONArray());
    }
  }

  /**
   Ensure JSONObject has JSONObject at specified key

   @param obj  in which to ensure JSONObject is present at attribute
   @param attr at which to ensure a JSONObject is present in object
   */
  static void ensureObjectAt(JSONObject obj, String attr) {
    if (!obj.has(attr)) {
      obj.put(attr, new JSONObject());
    }
  }

  /**
   Put a value into an JSONArray within the object JSONObject at the specified attr

   @param obj    within which to ensure the presence of a JSONArray, then put the value into that JSONArray
   @param attr   within the obj at which to ensure the presence of a JSONArray
   @param object to put into the sub-JSONArray
   */
  static void putInSubArray(JSONObject obj, String attr, Object object) {
    ensureArrayAt(obj, attr);
    obj.getJSONArray(attr).put(object);
  }

  /**
   Put a value into an JSONObject within the object JSONObject at the specified attr

   @param obj   within which to ensure the presence of a JSONObject, then put the value into that JSONObject
   @param attr  within the obj at which to ensure the presence of a JSONObject
   @param key   to put into the sub-JSONObject
   @param value to put into the sub-JSONObject
   */
  static void putInSubObject(JSONObject obj, String attr, String key, Object value) {
    ensureObjectAt(obj, attr);
    obj.getJSONObject(attr).put(key, value);
  }

}
