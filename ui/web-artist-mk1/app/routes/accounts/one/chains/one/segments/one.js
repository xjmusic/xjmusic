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
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('segment', params.segment_id, {reload: true})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.chains.one.segments');
      });
  }

});
