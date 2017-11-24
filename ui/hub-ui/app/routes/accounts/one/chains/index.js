// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';

import { get, set } from '@ember/object';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject: player service
  player: service(),

  /**
   * Route Model
   * @returns {*}
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
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    set(this, 'routeHeadline', {
      title: model.account.get('name') + ' ' + 'Chains',
      entity: {
        name: 'Account',
        id: model.account.get('id')
      }
    });
  },

  /**
   Route Actions
   */
  actions: {
    play(chain) {
      this.get('player').play(chain);
    }
  },

});
