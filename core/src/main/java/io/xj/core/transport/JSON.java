// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import io.xj.core.config.Exposure;
import io.xj.core.model.Entity;
import io.xj.core.util.CamelCasify;

import org.jooq.Record;
import org.jooq.Result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

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

    /*
     Cache JSON key names for SQL column names
     SQL columns with null values here
     will not be exposed as JSON
    */
    String[] key = new String[maxCol];
    for (int i = 1; i < maxCol; i++) {
      key[i] = CamelCasify.ifNeeded(data.getColumnLabel(i));
    }

    while (rs.next()) {
      JSONObject obj = new JSONObject();

      for (int i = 1; i < maxCol; i++) {
        if (null != key[i]) {
          if (Types.ARRAY == data.getColumnType(i)) {
            obj.put(key[i], rs.getArray(i));
          } else if (Types.BIGINT == data.getColumnType(i)) {
            obj.put(key[i], rs.getInt(i));
          } else if (Types.BOOLEAN == data.getColumnType(i)) {
            obj.put(key[i], rs.getBoolean(i));
          } else if (Types.BLOB == data.getColumnType(i)) {
            obj.put(key[i], rs.getBlob(i));
          } else if (Types.DOUBLE == data.getColumnType(i)) {
            obj.put(key[i], rs.getDouble(i));
          } else if (Types.FLOAT == data.getColumnType(i)) {
            obj.put(key[i], rs.getFloat(i));
          } else if (Types.INTEGER == data.getColumnType(i)) {
            obj.put(key[i], rs.getInt(i));
          } else if (Types.NVARCHAR == data.getColumnType(i)) {
            obj.put(key[i], rs.getNString(i));
          } else if (Types.VARCHAR == data.getColumnType(i)) {
            obj.put(key[i], rs.getString(i));
          } else if (Types.TINYINT == data.getColumnType(i) || Types.SMALLINT == data.getColumnType(i)) {
            obj.put(key[i], rs.getInt(i));
          } else if (Types.DATE == data.getColumnType(i)) {
            obj.put(key[i], rs.getDate(i));
          } else if (Types.TIMESTAMP == data.getColumnType(i)) {
            obj.put(key[i], rs.getTimestamp(i));
          } else {
            obj.put(key[i], rs.getObject(i));
          }
        }
      }
      result.put(obj);
    }

    return result;
  }

  /**
   JSONArray from a jOOQ Result

   @param result to build array of
   @param <R>    extends jOOQ Record
   @return JSON array
   */
  static <R extends Record> JSONArray arrayOf(Result<R> result) {
    JSONArray out = new JSONArray();
    result.forEach(record -> out.put(objectFromRecord(record)));
    return out;
  }

  /**
   JSONArray from a single jOOQ Result

   @param items to put into array
   @return array of a single result
   */
  static <J extends Entity> JSONArray arrayOf(Iterable<J> items) throws Exception {
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
   JSONObject from a jOOQ Record

   @param record to construct output from
   @return JSON object
   */
  @Nullable
  static JSONObject objectFromRecord(Record record) {
    if (null == record) {
      return null;
    }
    JSONObject result = new JSONObject();
    record.intoMap().forEach((key, val) -> {
      String colName = CamelCasify.ifNeeded(key);
      if (null != colName) {
        result.put(colName, val);
      }
    });
    return result;
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
  static JSONObject objectFrom(Entity obj) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return new JSONObject(objectMapper.writeValueAsString(obj));
    } catch (JsonProcessingException e) {
      JSONObject err = new JSONObject();
      err.put("error", e.getOriginalMessage());
      return err;
    }
  }
}
