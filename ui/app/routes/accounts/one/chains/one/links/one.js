// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
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
    return this.store.findRecord('link', params.link_id)
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('accounts.one.chains.one.links');
      });
  },

  /**
   * Route Breadcrumb
   * @param model
   */
  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.getTitle()
    });
  }

});
