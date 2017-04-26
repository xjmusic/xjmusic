// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function () {
    let account = this.modelFor('accounts.one');
    let chains = this.store.query('chain', {accountId: account.get('id')})
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('');
      });
    return Ember.RSVP.hash({
      account: account,
      chains: chains,
    });
  },

  actions: {

    editChain(chain) {
      this.transitionTo('accounts.one.chains.one', chain);
    },

  }
});
