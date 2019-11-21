//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';
import {hash} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let self = this;
    return hash({
      index: this.store.findAll('account')
        .catch((error) => {
          get(self, 'display').error(error);
          self.transitionTo('');
        })
    }, 'accounts');
  },

  /**
   * Route Actions
   */
  actions: {}

});
