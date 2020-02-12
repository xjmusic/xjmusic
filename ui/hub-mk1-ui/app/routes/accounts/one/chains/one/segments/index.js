// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

const LOAD_SECONDS_BEFORE_NOW = 30;

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
  refreshSeconds: 7,

  /**
   Route Actions
   */
  actions: {
    follow(chain, segment) {
      this.player.follow(chain, segment);
    }
  },

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let chain = this.modelFor('accounts.one.chains.one');
    let account = this.modelFor('accounts.one');
    return new EmberPromise((resolve, reject) => {
      let queryParams = {
        chainId: chain.get('id')
      };
      if (this.player.isFollowing())
        queryParams["fromSecondsUTC"] = Math.floor(this.player.nowMillisUTC() / 1000) - LOAD_SECONDS_BEFORE_NOW;
      this.store.query(
        'segment', queryParams)
        .catch((error) => {
          console.error(error);
          get(this, 'display').error(error);
          reject(error);
          this.transitionTo('accounts.one.chains.one', chain.get('id'));
        })
        .then(segments => {
          this.findAllProgramsAndInstruments(segments)
            .catch((error) => {
              console.error(error);
              get(this, 'display').error(error);
              reject(error);
              this.transitionTo('accounts.one.chains.one', chain.get('id'));
            })
            .then(() => {
              resolve(hash({
                account: account,
                chain: chain,
                segments: segments
              }, 'chain, segments'));
            })
        });
    });
  },

  /**
   * Find all programs and instruments for the given segments, thereby caching them in the store
   * @param segments to find programs for
   * return {Promise}
   */
  findAllProgramsAndInstruments: function (segments) {
    return new EmberPromise((resolve/*, reject*/) => {
      let programMap = {};
      let instrumentMap = {};
      segments.forEach(segment => {
        segment.get('segmentChoices').forEach(choice => {
          let program = choice.get("program");
          programMap[program.get('id"')] = program;
          choice.get('segmentChoiceArrangements').forEach(arrangement => {
            let instrument = arrangement.get("instrument");
            instrumentMap[instrument.get('id"')] = instrument;
          })
        })
      });
      resolve();
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

    this.player.scrollToNowPlayingSegment(false);
  },

  /**
   On route deactivation, clear the refresh interval
   */
  deactivate() {
    clearInterval(this.refreshInterval);
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
