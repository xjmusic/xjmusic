// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
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
    return this.store.findAll('user')
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
      title: 'Users'
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editUser(user) {
      this.transitionTo('users.one', user);
    },

  },

});
