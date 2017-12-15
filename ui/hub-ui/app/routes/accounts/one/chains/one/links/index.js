// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { Promise as EmberPromise, hash } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  // Inject: chain-link player service
  player: service(),

  // for persisting the auto-refresh interval
  refreshInteval: null,

  // # of seconds between auto-refresh
  refreshSeconds: 5,

  /**
   Route Actions
   */
  actions: {
    play(chain, link) {
      this.get('player').play(chain, link);
    }
  },

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let self = this;
    let chain = this.modelFor('accounts.one.chains.one');
    return new EmberPromise((resolve, reject) => {
      let linkQuery = {
        chainId: chain.get('id'),
        include: 'memes,choices,arrangements,chords,messages',
      };
      let links = this.store.query(
        'link', linkQuery)
        .catch((error) => {
          get(self, 'display').error(error);
          reject(error);
          self.transitionTo('');
        });
      resolve(hash({
        chain: chain,
        links: links
      }));
    });
  },

  /**
   On route deactivation, clear the refresh interval
   */
  deactivate() {
    clearInterval(this.get('refreshInterval'));
    console.log("...auto-refresh Stopped.");
  },

  /**
   On route activation, set the refresh interval
   */
  activate() {
    console.log("Started auto-refresh...");
    let self = this;
    self.set('refreshInterval', setInterval(function () {
      console.log("Auto-refresh now!");
      self.send("sessionChanged");
    }, self.refreshSeconds * 1000));
  },


});
