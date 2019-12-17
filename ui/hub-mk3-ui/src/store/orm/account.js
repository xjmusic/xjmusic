// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, Model} from "redux-orm";

class Account extends Model {
  toString() {
    return `Account: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

Account.modelName = 'Account';

// Declare your related fields.
Account.fields = {
  id: attr(),
  name: attr(),
  /*
    users: many('account-user'),
    libraries: many('library'),
    chains: many('chain'),
  */
};

export default Account;
