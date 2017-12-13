// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Component from '@ember/component';

/**
 * Displays a Chain state-change U.I.
 */
const ChainStateChangeComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Component Actions
   */
  actions: {

    /**
     * Update Chain State, with optional confirmation dialog
     *
     * @param {String} toState
     * @param {bool} askConfirm whether to show a confirmation dialoge first
     */
    changeState(toState, askConfirm) {
      let self = this;
      let model = this.get('model');

      if (askConfirm && !confirm(this.msgConfirm(toState))) {
        get(self, 'display').warning('Cancelled.');
        return;
      }

      model.set('state', toState);
      model.save().then(
        () => {
          get(self, 'display').success(self.msgSuccess());
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
    let model = this.get('model');
    return [
      model.get('name'),
      'is now in',
      model.get('state'),
      'state'
    ].join(' ');
  },

  /**
   * Confirmation message
   *
   * @param {String} toState
   * @returns {string}
   */
  msgConfirm(toState) {
    let model = this.get('model');
    return [
      'Really change',
      model.get('name'),
      'to',
      toState,
      'state?'
    ].join(' ');
  }

});

/**
 * Usage (e.g, in Handlebars, where chain model is "myChainModel"):
 *
 *   {{chain-state-change myChainModel}}
 */
ChainStateChangeComponent.reopenClass({
  positionalParams: ['model']
});

export default ChainStateChangeComponent;
