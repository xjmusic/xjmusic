//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class UserSerializer implements JsonSerializer<User> {
  private static final String ID = "id";
  private static final String NAME = "name";
  private static final String AVATAR_URL = "avatarUrl";
  private static final String EMAIL = "email";
  private static final String ROLES = "roles";

  @Override
  public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject obj = new JsonObject();
    obj.add(ID, context.serialize(src.getId()));
    obj.add(NAME, context.serialize(src.getName()));
    obj.add(AVATAR_URL, context.serialize(src.getAvatarUrl()));
    obj.add(EMAIL, context.serialize(src.getEmail()));
    obj.add(ROLES, context.serialize(src.getRoles()));
    return obj;
  }
}
