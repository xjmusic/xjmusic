// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  model() {
    let account = this.modelFor('accounts.account');
    return Ember.RSVP.hash({
      account: account,
      users: this.store.findAll('user'),
      userToAdd: null,
      accountUsers: this.store.query('account-user', { account: account.id }),
    });
  },

  actions: {

    sessionChanged: function() {
      this.refresh();
    },

  },

});
