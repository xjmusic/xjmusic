// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let account = this.modelFor('accounts.one');
    return Ember.RSVP.hash({
      account: account,
      users: this.store.findAll('user'),
      userToAdd: null,
      accountUsers: this.store.query('account-user', { accountId: account.id }),
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.account.get('name') + ' ' + 'Users',
      entity: {
        name: 'Account',
        id: model.account.get('id')
      }
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
