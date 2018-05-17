// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import { Promise as EmberPromise } from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: flash message service
  display: service(),

  // Inject: config service
  config: service(),

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
          reject('Could not instantiate new Pattern', error);
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
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
      let sequenceType = sequence.get('type');
      let patternType;
      switch (sequenceType) {
        case 'Macro':
        case 'Main':
          patternType = sequenceType;
          break;

        default:
          patternType = config.patternDetailTypes[0];
          break;
      }
      return this.store.createRecord('pattern', {
        sequence: sequence,
        type: patternType,
        total: 0 // otherwise risk sending null with macro-type sequence, see BUG [#246]
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createPattern(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Created pattern ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    },

    cancelCreatePattern() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.sequences.one.patterns');
        }
      }
    }
  }

});
