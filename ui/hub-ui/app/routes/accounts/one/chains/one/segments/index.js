// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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

  // Inject: chain-segment player service
  player: service(),

  // Inject: segment scroll service
  segmentScroll: service(),

  // for persisting the auto-refresh interval
  refreshInterval: null,

  // # of seconds between auto-refresh
  refreshSeconds: 5,

  /**
   Route Actions
   */
  actions: {
    play(chain, segment) {
      this.get('player').play(chain, segment);
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
      let segmentQuery = {
        chainId: chain.get('id'),
        include: 'memes,choices,arrangements,chords,messages',
      };
      let segments = this.store.query(
        'segment', segmentQuery)
        .catch((error) => {
          get(self, 'display').error(error);
          reject(error);
          self.transitionTo('');
        });
      resolve(hash({
        chain: chain,
        segments: segments
      }));
    });
  },

  /**
   Spy on render template,
   in order to trigger segment scroll
   * @param controller
   * @param model
   */
  renderTemplate(controller, model) {
    this._super(controller, model);

    this.get('player').scrollToNowPlayingSegment(false);
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
