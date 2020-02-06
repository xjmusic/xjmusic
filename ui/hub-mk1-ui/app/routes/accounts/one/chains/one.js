// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject: chain-segment player service
  player: service(),

  /**
   Route Actions
   */
  actions: {
    follow(chain) {
      this.player.follow(chain, null);
    },
    stop() {
      this.player.stop();
    }
  },

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('chain', params['chain_id'], {reload: true})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.chains');
      });
  }

});
