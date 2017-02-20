// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let chain = this.modelFor('accounts.one.chains.one');
    let link =this.modelFor('accounts.one.chains.one.links.one');
    return Ember.RSVP.hash({
      chain: chain,
      link: link,
    });
  },

});
