// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.output;

import io.outright.xj.core.tables.records.UserRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface JSONOutputProvider {
  /**
   * Build a JSONObject with root property containing array of records, from a standard JDBC ResultSet.
   * @param rootName root property name
   * @param data ResultSet to convert into JSON records
   * @return JSONObject
   * @throws SQLException if SQL failure
   * @throws JSONException if JSON failure
   */
  JSONObject ListOf(String rootName, ResultSet data) throws SQLException, JSONException;

  /**
   * Build a JSONObject with root property containing a record, from a map of SQL columns and values.
   * @param rootName root property name
   * @param data SQL columns and row values
   * @return JSONObject
   */
  JSONObject Record(String rootName, Map<String, Object> data);

  /**
   * Build a JSONObject with root property containing an error message.
   * @link http://jsonapi.org/format/#error-objects
   * @param message for single error message
   * @return JSONObject
   */
  JSONObject Error(String message);
}
