// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get, set } from '@ember/object';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {Promise.<T>}
   */
  model: function () {
    let self = this;
    return this.store.findAll('user')
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
  },

  /**
   * Headline
   */
  afterModel() {
    set(this, 'routeHeadline', {
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
