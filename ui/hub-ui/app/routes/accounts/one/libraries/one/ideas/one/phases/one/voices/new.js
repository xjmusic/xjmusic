// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
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
    if (auth.isArtist || auth.isAdmin) {
      return this.store.createRecord('voice', {
        type: config.voiceTypes[0],
        phase: this.modelFor('accounts.one.libraries.one.ideas.one.phases.one')
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices');
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
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one', model);
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

    cancelCreateVoice(transition)
    {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices');
        } else {
          transition.abort();
        }
      }
    }
  }

});
