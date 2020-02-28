/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class AccountUser extends Model {
  toString() {
    return `AccountUser: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

AccountUser.modelName = 'AccountUser';

// Declare your related fields.
AccountUser.fields = {
  id: attr(),
  accountId: fk({
    to: 'Account',
    as: 'account',
    relatedName: 'accountUsers',
  }),
  userId: fk({
    to: 'User',
    as: 'user',
    relatedName: 'accountUsers',
  }),
};

export default AccountUser;
