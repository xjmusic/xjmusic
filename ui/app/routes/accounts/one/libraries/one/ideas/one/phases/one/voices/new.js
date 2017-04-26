// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: configuration service
  config: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    return new Ember.RSVP.Promise((resolve, reject) => {
      let self = this;
      Ember.get(this, 'config').promises.config.then(
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

  actions: {

    createVoice(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Created "' + model.get('description') + '" voice.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
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
