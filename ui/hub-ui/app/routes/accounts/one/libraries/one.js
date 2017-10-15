// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('library', params.library_id)
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries');
      });
  },

  /**
   * Route breadcrumb
   * @param model
   */
  afterModel(model) {
    Ember.set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
