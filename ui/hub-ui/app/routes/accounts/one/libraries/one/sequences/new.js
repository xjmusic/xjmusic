// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';
import $ from 'jquery';

import {Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
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
          reject('Could not instantiate new Sequence', error);
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

      let sequence = this.store.createRecord('sequence', {
        type: config.sequenceTypes[0],
        library: this.modelFor('accounts.one.libraries.one')
      });

      // resolves the user *after* closure
      this.store.findRecord('user', this.get('auth').userId).then((record) => {
        sequence.set('user', record);
      });

      return sequence;
    } else {
      this.transitionTo('accounts.one.libraries.one.sequences');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createSequence(model) {
      let generateLibrarySupersequence = $('#generateLibrarySupersequence:checked').length > 0;
      if (generateLibrarySupersequence) {
        $('.generate-supersequence').html('<div class="loader">&nbsp;</div>');
      }
      model.save({
        adapterOptions: {
          query: {
            generateLibrarySupersequence: generateLibrarySupersequence
          }
        }
      }).then(
        () => {
          if (generateLibrarySupersequence) {
            get(this, 'display').success('Generate library supersequence ' + model.get('name') + '.');
          } else {
            get(this, 'display').success('Created sequence ' + model.get('name') + '.');
          }
          this.transitionTo('accounts.one.libraries.one.sequences.one', model);
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

    cancelCreateSequence() {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.sequences');
        }
      }
    }

  }
});
