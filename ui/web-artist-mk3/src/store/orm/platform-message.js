/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, Model} from "redux-orm";

class PlatformMessage extends Model {
  toString() {
    return `[${this.type}] ${this.body}`;
  }

  // Declare any static or instance methods you need.
}

PlatformMessage.modelName = 'PlatformMessage';

// Declare your related fields.
PlatformMessage.fields = {
  id: attr(),
  body: attr(),
  type: attr(),
  createdAt: attr(),
  updatedAt: attr(),
};

export default PlatformMessage;
