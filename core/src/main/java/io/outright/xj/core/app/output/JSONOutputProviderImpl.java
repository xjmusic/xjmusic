// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.output;

import io.outright.xj.core.app.config.Config;

import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class JSONOutputProviderImpl implements JSONOutputProvider {
//  private final static Logger log = LoggerFactory.getLogger(JSONOutputProviderImpl.class);

  @Inject
  public JSONOutputProviderImpl(){}

  @Override
  public JSONObject ListOf(String rootName, ResultSet rs) throws SQLException, JSONException {
    JSONArray jsonSub = new JSONArray();
    ResultSetMetaData data = rs.getMetaData();
    int maxCol = data.getColumnCount() + 1;

    /*
     Cache JSON key names for SQL column names
     SQL columns with null values here
     will not be exposed as JSON
    */
    String[] key = new String[maxCol];
    for (int i = 1; i < maxCol; i++) {
      key[i] = jsonNameOfColumn(data.getColumnName(i));
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

      jsonSub.put(obj);
    }

    JSONObject json = new JSONObject();
    json.put(rootName, jsonSub);

    return json;
  }

  private String jsonNameOfColumn(String columnName) {
    return Config.jsonNameForSqlColumn.get(columnName);
  }

}
