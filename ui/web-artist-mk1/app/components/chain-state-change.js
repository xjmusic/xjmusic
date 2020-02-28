/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Component from '@ember/component';

/**
 * Displays a Chain state-change U.I.
 */
const ChainStateChangeComponent = Component.extend({

    // Inject: flash message service
    display: service(),

    // Inject: ember data store service
    store: service(),

    /**
     * Component Actions
     */
    actions: {

      /**
       * Revive a Chain
       * @param {bool} askConfirm whether to show a confirmation dialogue first
       */
      revive(askConfirm) {
        let self = this;
        let model = this.model;
        let reviveId = model.get('id');

        if (askConfirm && !confirm(this.msgConfirmRevive())) {
          get(self, 'display').warning('Cancelled.');
          return;
        }
        let createNewChain = get(self, 'store').createRecord('chain', {});
        createNewChain.save({
          adapterOptions: {
            query: {
              reviveId: reviveId
            }
          }
        }).then(
          (createdChain) => {
            get(self, 'display').success('Revived Chain #' + createdChain.get('id') + ' from prior Chain #' + reviveId);
            self.submit();
          },
          (error) => {
            this.display.error(error);
          });
      },

      /**
       * Update Chain State, with optional confirmation dialog
       *
       * @param {String} toState
       * @param {bool} askConfirm whether to show a confirmation dialogue first
       */
      changeState(toState, askConfirm) {
        let self = this;
        let model = this.model;

        if (askConfirm && !confirm(this.msgConfirmChange(toState))) {
          get(self, 'display').warning('Cancelled.');
          return;
        }

        model.set('state', toState);
        model.save().then(
          () => {
            get(self, 'display').success(self.msgSuccess());
            self.submit();
          },
          (error) => {
            get(self, 'model').rollbackAttributes();
            get(self, 'display').error(error);
          });
      }

    },

    /**
     * Success message
     *
     * @returns {string}
     */
    msgSuccess() {
      let model = this.model;
      return [
        model.get('name'),
        'is now in',
        model.get('state'),
        'state'
      ].join(' ');
    }
    ,

    /**
     * Confirmation message to change state of a chain
     *
     * @param {String} toState
     * @returns {string}
     */
    msgConfirmChange(toState) {
      let model = this.model;
      return [
        'Really change',
        model.get('name'),
        'to',
        toState,
        'state?'
      ].join(' ');
    }
    ,

    /**
     * Confirmation message to revive a chain
     *
     * @returns {string}
     */
    msgConfirmRevive() {
      let model = this.model;
      return 'Really revive Chain #' + model.get('id') + '?';
    }

  })
;

/**
 * Usage (e.g, in Handlebars, where chain model is "myChainModel"):
 *
 *   {{chain-state-change myChainModel}}
 */
ChainStateChangeComponent.reopenClass({
  positionalParams: ['model']
});

export default ChainStateChangeComponent;
