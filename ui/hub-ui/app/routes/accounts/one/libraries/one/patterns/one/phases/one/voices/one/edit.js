// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import {get} from '@ember/object';

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
        () => {
          resolve(self.resolvedModel());
        },
        (error) => {
          reject('Could not instantiate new Voice', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let phase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
      let voice = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one');
      voice.set('phase', phase);
      return voice;
    } else {
      this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.voices');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveVoice(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated "' + model.get('description') + '" voice.');
          history.back();
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyVoice(model) {
      let phase = model.get('phase');
      let pattern = phase.get('pattern');
      let library = pattern.get('library');
      let account = library.get('account');
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Deleted "' + model.get('description') + '" voice.');
          this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.voices', account, library, pattern, phase);
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
    }
  }

});
