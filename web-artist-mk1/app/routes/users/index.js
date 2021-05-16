/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
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
   * Route Actions
   */
  actions: {

    editUser(user) {
      this.transitionTo('users.one', user);
    },

  },

});
