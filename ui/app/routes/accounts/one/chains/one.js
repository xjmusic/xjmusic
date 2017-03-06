// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model(params) {
    return this.store.findRecord('chain', params.chain_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.chains');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
