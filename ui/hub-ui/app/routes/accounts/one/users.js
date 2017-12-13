// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { hash } from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let account = this.modelFor('accounts.one');
    return hash({
      account: account,
      users: this.store.findAll('user'),
      userToAdd: null,
      accountUsers: this.store.query('account-user', { accountId: account.id }),
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function() {
      this.refresh();
    },

  },

});
