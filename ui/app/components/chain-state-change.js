// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

/**
 * Displays a Chain state-change U.I.
 */
const ChainStateChangeComponent = Ember.Component.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

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
        Ember.get(self, 'display').warning('Cancelled.');
        return;
      }

      model.set('state', toState);
      model.save().then(
        () => {
          Ember.get(self, 'display').success(self.msgSuccess());
        },
        (error) => {
          Ember.get(self, 'display').error(error);
        });
    }

  },

  /**
   * Success message
   *
   * @returns {string}
   */
  msgSuccess () {
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
  msgConfirm (toState) {
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
