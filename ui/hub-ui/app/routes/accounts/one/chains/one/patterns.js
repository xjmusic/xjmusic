// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
        () => {
          resolve(self.resolvedModel());
        },
        (error) => {
          reject('Could not instantiate new Chain', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let chain = this.modelFor('accounts.one.chains.one');
    let account = this.modelFor('accounts.one');
    return hash({
      chain: chain,
      patterns: this.store.query('pattern', {accountId: account.id}),
      patternToAdd: null,
      chainPatterns: this.store.query('chain-pattern', {chainId: chain.id}),
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    removePattern(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Pattern from Chain.');
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    addPattern(model) {
      let chainConfig = this.store.createRecord('chain-pattern', {
        chain: model.chain,
        pattern: model.patternToAdd,
      });
      chainConfig.save().then(
        () => {
          get(this, 'display').success('Added ' + model.patternToAdd.get('name') + ' to ' + model.chain.get('name') + '.');
          // this.transitionToRoute('chains.one.patterns',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
