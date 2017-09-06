// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {Promise.<T>}
   */
  model: function () {
    let self = this;
    return this.store.findAll('account')
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
  },

  /**
   * Headline
   */
  afterModel() {
    Ember.set(this, 'routeHeadline', {
      title: 'Accounts'
    });
  },

  /**
   * Route Actions
   */
  actions: {}

});
