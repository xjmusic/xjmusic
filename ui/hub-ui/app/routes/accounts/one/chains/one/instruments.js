// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
      instruments: this.store.query('instrument', {accountId: account.id}),
      instrumentToAdd: null,
      chainInstruments: this.store.query('chain-instrument', {chainId: chain.id}),
    });
  },


  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    removeInstrument(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Instrument from Chain.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addInstrument(model) {
      let chainConfig = this.store.createRecord('chain-instrument', {
        chain: model.chain,
        instrument: model.instrumentToAdd,
      });
      chainConfig.save().then(
        () => {
          get(this, 'display').success('Added ' + model.instrumentToAdd.get('description') + ' to ' + model.chain.get('name') + '.');
          // this.transitionToRoute('chains.one.instruments',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
