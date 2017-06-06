// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.user;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.role.Role;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;

import com.google.api.client.util.Maps;

import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.User.USER;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class User extends Entity {

  public static final Field<String> FIELD_ROLES = DSL.field(DSL.name("table", "column"), String.class);

  // JSON output keys
  public static final String KEY_ONE = "user";
  public static final String KEY_MANY = "users";
  public static final String KEY_ROLES = Role.KEY_MANY;
  public static final String KEY_EMAIL = "email";
  public static final String KEY_NAME = "name";
  public static final String KEY_AVATAR_URL = "avatarUrl";
  // Roles
  private String roles;
  private String email;
  private String avatarUrl;
  private String name;

  public String getRoles() {
    return roles;
  }

  public User setRoles(String roles) {
    this.roles = roles;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public User setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public User setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
    return this;
  }

  public String getName() {
    return name;
  }

  public User setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Return User as JSON object, including virtual field "roles"

   @return user as JSON
   */
  public JSONObject toJSONObject() {
    JSONObject obj = new JSONObject();
    obj.put(KEY_ID, id);
    obj.put(KEY_EMAIL, email);
    obj.put(KEY_AVATAR_URL, avatarUrl);
    obj.put(KEY_NAME, name);
    obj.put(KEY_ROLES, roles);
    return obj;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.getRoles() == null || this.getRoles().length() == 0) {
      throw new BusinessException("User roles required.");
    }
  }

  @Override
  public User setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(USER.ID);
    email = record.get(USER.EMAIL);
    avatarUrl = record.get(USER.AVATAR_URL);
    name = record.get(USER.NAME);
    roles = record.get(Role.KEY_MANY).toString();
    createdAt = record.get(USER.CREATED_AT);
    updatedAt = record.get(USER.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(USER.NAME, name);
    fieldValues.put(USER.AVATAR_URL, avatarUrl);
    fieldValues.put(USER.EMAIL, email);
    return fieldValues;
  }
}
