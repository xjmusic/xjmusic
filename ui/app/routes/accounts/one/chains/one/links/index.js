// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function() {
    let chain = this.modelFor('accounts.one.chains.one');
    let links = this.store.query('link', { chainId: chain.get('id') }).catch((error)=>{
      Ember.get(this, 'display').error(error);
      this.transitionTo('');
    });
    return Ember.RSVP.hash({
      chain: chain,
      links: links,
    });
  },

  actions: {}

});
