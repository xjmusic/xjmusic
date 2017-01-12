// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.output;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface JSONOutputProvider {
  /**
   * Build a JSONArray from a standard JDBC ResultSet
   * @param rootName String root property name
   * @param rs ResultSet to convert into JSON records
   * @return JSONArray
   * @throws SQLException if SQL failure
   * @throws JSONException if JSON failure
   */
  JSONObject ListOf(String rootName, ResultSet rs) throws SQLException, JSONException;
}
