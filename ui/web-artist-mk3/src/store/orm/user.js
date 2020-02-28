/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, Model} from "redux-orm";

class User extends Model {
  toString() {
    return `User: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

User.modelName = 'User';

// Declare your related fields.
User.fields = {
  id: attr(),
  name: attr(),
  avatarUrl: attr(),
  email: attr(),
  roles: attr()
};

export default User;
