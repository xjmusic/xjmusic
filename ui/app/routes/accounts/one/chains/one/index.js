// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let account = this.modelFor('accounts.one');
    let chain =this.modelFor('accounts.one.chains.one');
    return Ember.RSVP.hash({
      account: account,
      chain: chain,
    });
  },

});
