// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.transport;

import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.util.CamelCasify;

import org.jooq.Record;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public abstract class JSON {

  public static JSONArray arrayFromResultSet(ResultSet rs) throws SQLException, JSONException {
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
        if (key[i] != null) {
          if (data.getColumnType(i) == java.sql.Types.ARRAY) {
            obj.put(key[i], rs.getArray(i));
          } else if (data.getColumnType(i) == java.sql.Types.BIGINT) {
            obj.put(key[i], rs.getInt(i));
          } else if (data.getColumnType(i) == java.sql.Types.BOOLEAN) {
            obj.put(key[i], rs.getBoolean(i));
          } else if (data.getColumnType(i) == java.sql.Types.BLOB) {
            obj.put(key[i], rs.getBlob(i));
          } else if (data.getColumnType(i) == java.sql.Types.DOUBLE) {
            obj.put(key[i], rs.getDouble(i));
          } else if (data.getColumnType(i) == java.sql.Types.FLOAT) {
            obj.put(key[i], rs.getFloat(i));
          } else if (data.getColumnType(i) == java.sql.Types.INTEGER) {
            obj.put(key[i], rs.getInt(i));
          } else if (data.getColumnType(i) == java.sql.Types.NVARCHAR) {
            obj.put(key[i], rs.getNString(i));
          } else if (data.getColumnType(i) == java.sql.Types.VARCHAR) {
            obj.put(key[i], rs.getString(i));
          } else if (data.getColumnType(i) == java.sql.Types.TINYINT) {
            obj.put(key[i], rs.getInt(i));
          } else if (data.getColumnType(i) == java.sql.Types.SMALLINT) {
            obj.put(key[i], rs.getInt(i));
          } else if (data.getColumnType(i) == java.sql.Types.DATE) {
            obj.put(key[i], rs.getDate(i));
          } else if (data.getColumnType(i) == java.sql.Types.TIMESTAMP) {
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

  @Nullable
  public static JSONObject objectFromRecord(Record record) {
    if (record == null) {
      return null;
    }
    JSONObject result = new JSONObject();
    record.intoMap().forEach((k,v)->{
      String colName = CamelCasify.ifNeeded(k);
      if (colName != null) {
        result.put(colName,v);
      }
    });
    return result;
  }

  public static JSONObject wrap(String rootName, JSONObject data) {
    JSONObject result = new JSONObject();
    result.put(rootName, data);
    return result;
  }

  public static JSONObject wrap(String rootName, JSONArray data) {
    JSONObject result = new JSONObject();
    result.put(rootName, data);
    return result;
  }

  public static JSONObject wrapError(String message) {
    JSONObject error = new JSONObject();
    error.put(Exposure.KEY_ERROR_DETAIL, message);

    JSONArray errorsArr = new JSONArray();
    errorsArr.put(error);

    return wrap(Exposure.KEY_ERRORS, errorsArr);
  }
}
