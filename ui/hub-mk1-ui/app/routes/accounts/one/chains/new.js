// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

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
   * @returns {Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      let self = this;
      this.config.getConfig().then(
        () => {
          resolve(hash({
            chain: self.store.createRecord('chain', {
              account: self.modelFor('accounts.one'),
              state: self.config.chainStates[0],
              type: 'Production'
            })
          }, 'new chain'));
        },
        (error) => {
          reject('Could not instantiate new Chain', error);
        }
      );
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createChain(model) {
      model.save().then(
        (data) => {
          this.display.success('Created chain ' + data.get('name') + '.');
          this.transitionTo('accounts.one.chains.one', data);
        },
        (error) => {
          this.display.error(error);
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

    cancel() {
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
