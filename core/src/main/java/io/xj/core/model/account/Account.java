// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;

import org.jooq.Field;
import org.jooq.Record;

import com.google.api.client.util.Maps;

import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.Account.ACCOUNT;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object

 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.

 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public class Account extends Entity {

  /**
   For use in maps.
   */
  public static final String KEY_ONE = "account";
  public static final String KEY_MANY = "accounts";
  // Name
  private String name;

  public String getName() {
    return name;
  }

  public Account setName(String name) {
    this.name = name;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (this.name == null || this.name.length() == 0) {
      throw new BusinessException("Account name is required.");
    }
  }

  @Override
  public Account setFromRecord(Record record) {
    if (Objects.isNull(record)) {
      return null;
    }
    id = record.get(ACCOUNT.ID);
    name = record.get(ACCOUNT.NAME);
    createdAt = record.get(ACCOUNT.CREATED_AT);
    updatedAt = record.get(ACCOUNT.UPDATED_AT);
    return this;
  }

  @Override
  public Map<Field, Object> updatableFieldValueMap() {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(ACCOUNT.NAME, name);
    return fieldValues;
  }

}
