// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  // for keeping track of the auto-refresh interval
  refreshInteval: 0,

  // # of seconds between auto-refresh
  refreshSeconds: 10,

/**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let self = this;
    let chain = this.modelFor('accounts.one.chains.one');
    let links = this.store.query(
      'link', {
        chainId: chain.get('id'),
        include: 'memes,choices,chords,messages'
      })
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      chain: chain,
      links: links,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Chain Links',
      detail: {
        startAt: model.chain.get('startAt'),
        stopAt: model.chain.get('stopAt')
      },
      entity: {
        name: 'Chain',
        id: model.chain.get('id'),
        state: model.chain.get('state')
      }
    });
  },

  /**
   On route deactivation, clear the refresh interval
   */
  deactivate() {
    clearInterval(this.refreshInteval);
    console.log("...auto-refresh Stopped.");
  },

  /**
   On route activation, set the refresh interval
   */
  activate() {
    console.log("Started auto-refresh...");
    let self = this;
    this.refreshInteval = setInterval(function() {
      console.log("Auto-refresh now!");
      self.send("sessionChanged");
    }, self.refreshSeconds * 1000);
  },

  /**
   * Route Actions
   */
  actions: {}

});
