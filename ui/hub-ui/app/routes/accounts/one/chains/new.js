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
   * @returns {*} hash
   */
  resolvedModel(config) {
    let account = this.modelFor('accounts.one');
    return hash({
      chain: this.store.createRecord('chain', {
        account: account,
        state: config.chainStates[0],
        type: config.chainTypes[0]
      })
    }, 'new chain');
  },

  /**
   * Route Actions
   */
  actions: {

    createChain(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Created chain ' + model.get('name') + '.');
          this.transitionTo('accounts.one.chains.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = this.controller.get('model.chain');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    },

    cancelCreateChain() {
      let model = this.controller.get('model.chain');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.chains');
        }
      }
    }

  },

});
