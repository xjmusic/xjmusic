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
          reject('Could not instantiate new Sequence', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    return hash({
      sequence: sequence,
      patternToAdd: null,
      offsetToAdd: 0,
      patterns: this.store.query('pattern', {sequenceId: sequence.id}),
      sequencePatterns: this.store.query('sequencePattern', {sequenceId: sequence.id}),
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
          get(this, 'display').success('Removed Pattern from Sequence.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addPattern(model) {
      let sequenceConfig = this.store.createRecord('sequence-pattern', {
        sequence: model.sequence,
        pattern: model.patternToAdd,
        offset: model.offsetToAdd
      });
      sequenceConfig.save().then(
        () => {
          get(this, 'display').success('Added ' + model.patternToAdd.get('description') + ' to ' + model.sequence.get('name') + ' at offset ' + model.offsetToAdd + '.');
          // this.transitionToRoute('sequences.one.patterns',model.sequence);
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
