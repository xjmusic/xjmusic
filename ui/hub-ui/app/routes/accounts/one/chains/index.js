//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {hash} from 'rsvp';

import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject: player service
  player: service(),

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let self = this;
    let account = this.modelFor('accounts.one');
    let chains = this.store.query('chain', {accountId: account.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      account: account,
      chains: chains,
    }, 'chains and their parent account');
  },

  /**
   Route Actions
   */
  actions: {
    play(chain) {
      this.player.play(chain);
    }
  },

});
