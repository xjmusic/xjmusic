//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
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
   * @returns {*} hash
   */
  resolvedModel() {
    let chain = this.modelFor('accounts.one.chains.one');
    let account = this.modelFor('accounts.one');
    return hash({
      chain: chain,
      sequences: this.store.query('sequence', {accountId: account.id}),
      sequenceToAdd: null,
      chainSequences: this.store.query('chain-sequence', {chainId: chain.id}),
    }, 'chain, chain sequences, all available sequences, sequence to add to chain');
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    removeSequence(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Sequence from Chain.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addSequence(model) {
      let chainConfig = this.store.createRecord('chain-sequence', {
        chain: model.chain,
        sequence: model.sequenceToAdd,
      });
      chainConfig.save().then(
        () => {
          get(this, 'display').success('Added ' + model.sequenceToAdd.get('name') + ' to ' + model.chain.get('name') + '.');
          // this.transitionToRoute('chains.one.sequences',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
