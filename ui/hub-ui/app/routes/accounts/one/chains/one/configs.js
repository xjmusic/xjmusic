// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { Promise as EmberPromise, hash } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      let self = this;
      get(this, 'config').promises.config.then(
        (config) => {
          resolve(self.resolvedModel(config));
        },
        (error) => {
          reject('Could not instantiate new Chain', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @param config
   * @returns {*}
   */
  resolvedModel(config) {
    let chain = this.modelFor('accounts.one.chains.one');
    return hash({
      chain: chain,
      chainConfigToAdd: this.store.createRecord('chain-config', {
        chain: chain,
        type: ''
      }),
      chainConfigTypes: config.chainConfigTypes,
      chainConfigs: this.store.query('chain-config', {chainId: chain.id}),
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    removeConfig(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Config from Chain.');
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    addConfig(model) {
      let self = this;
      let chainConfig = model.chainConfigToAdd;
      chainConfig.save().then(
        (saved) => {
          get(self, 'display').success('Added ' + saved.get('type') + '=' + saved.get('value'));
          // this.transitionToRoute('chains.one.configs',model.chain);
          self.send("sessionChanged");
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },

  },

});
