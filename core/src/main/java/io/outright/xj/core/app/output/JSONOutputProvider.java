// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.output;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface JSONOutputProvider {
  /**
   * Build a JSONArray of records from a standard JDBC ResultSet.
   * @param data ResultSet to convert into JSON records
   * @return JSONArray
   * @throws SQLException if SQL failure
   * @throws JSONException if JSON failure
   */
  JSONArray arrayFromResultSet(ResultSet data) throws SQLException, JSONException;

  /**
   * Build a JSONObject containing a record, from a map of SQL columns and values.
   * @param objectMap to build object from
   * @return JSONObject
   */
  JSONObject objectFromMap(Map<String, Object> objectMap);

  /**
   * Build a JSONObject with root property containing a JSONObject
   * @param rootName root property name
   * @param data inner JSONObject
   * @return JSONObject
   */
  JSONObject wrap(String rootName, JSONObject data);

  /**
   * Build a JSONObject with root property containing a JSONArray
   * @param rootName root property name
   * @param data inner JSONArray
   * @return JSONObject
   */
  JSONObject wrap(String rootName, JSONArray data);

  /**
   * Build a JSONObject with root property containing an error message.
   * @link http://jsonapi.org/format/#error-objects
   * @param message for single error message
   * @return JSONObject
   */
  JSONObject wrapError(String message);
}
