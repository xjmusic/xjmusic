// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ChainConfig extends Model {
  toString() {
    return `ChainConfig: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ChainConfig.modelName = 'ChainConfig';

// Declare your related fields.
ChainConfig.fields = {
  id: attr(),
  type: attr(),
  value: attr(),
  chainId: fk({
    to: 'Chain',
    as: 'chain',
    relatedName: 'chainConfigs',
  }),
};

export default ChainConfig;
