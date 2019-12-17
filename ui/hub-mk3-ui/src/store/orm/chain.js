// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class Chain extends Model {
  toString() {
    return `Chain: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

Chain.modelName = 'Chain';

// Declare your related fields.
Chain.fields = {
  id: attr(),
  name: attr(),
  state: attr(),
  type: attr(),
  startAt: attr(),
  stopAt: attr(),
  embedKey: attr(),
  accountId: fk({
    to: 'Account',
    as: 'account',
    relatedName: 'chains',
  }),
  /*
  segments: many('segment'),
  chainConfigs: many('chain-config'),
  chainBindings: many('chain-binding'),
   */
};

export default Chain;
