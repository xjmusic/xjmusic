// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('chain', params.chain_id)
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('accounts.one.chains');
      });
  },

  /**
   * Route Breadcrumb
   * @param model
   */
  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
