// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { Promise as EmberPromise } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

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
          reject('Could not instantiate new Voice', error);
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
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    if (auth.isArtist || auth.isAdmin) {
      return this.store.createRecord('voice', {
        type: config.voiceTypes[0],
        sequence: sequence
      });
    } else {
      let library = sequence.get('library');
      let account = sequence.get('account');
      this.transitionTo('accounts.one.libraries.one.sequences.one.voices', account, library, sequence);
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createVoice(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Created "' + model.get('description') + '" voice.');
          this.transitionTo('accounts.one.libraries.one.sequences.one.voices.one', model);
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

    cancelCreateVoice() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.sequences.one.voices');
        }
      }
    }
  }

});
