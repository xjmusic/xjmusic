//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get, set } from '@ember/object';

import { inject as service } from '@ember/service';
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
    play(chain) {
      this.get('player').play(chain, null);
    },
    stop() {
      this.get('player').stop();
    }
  },

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('chain', params.chain_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.chains');
      });
  },

  /**
   * Route Breadcrumb
   * @param model
   */
  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
