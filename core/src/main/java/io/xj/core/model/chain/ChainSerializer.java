//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class ChainSerializer implements JsonSerializer<Chain> {
  private static final String ID = "id";
  private static final String STATE = "state";
  private static final String CREATED_AT = "createdAt";
  private static final String UPDATED_AT = "updatedAt";
  private static final String START_AT = "startAt";
  private static final String ACCOUNT_ID = "accountId";
  private static final String TYPE = "type";
  private static final String NAME = "name";
  private static final String STOP_AT = "stopAt";
  private static final String EMBED_KEY = "embedKey";

  @Override
  public JsonElement serialize(Chain src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(ID, context.serialize(src.getId()));
    obj.add(STATE, context.serialize(src.getState()));
    obj.add(ACCOUNT_ID, context.serialize(src.getAccountId()));
    obj.add(TYPE, context.serialize(src.getType()));
    obj.add(NAME, context.serialize(src.getName()));
    obj.add(START_AT, context.serialize(src.getStartAt()));
    if (Objects.nonNull(src.getStopAt()))
      obj.add(STOP_AT, context.serialize(src.getStopAt()));
    obj.add(EMBED_KEY, context.serialize(src.getEmbedKey()));
    obj.add(CREATED_AT, context.serialize(src.getCreatedAt()));
    obj.add(UPDATED_AT, context.serialize(src.getUpdatedAt()));
    return obj;
  }
}
