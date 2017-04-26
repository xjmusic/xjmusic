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
      let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
      let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
      voice.set('phase', phase);
      return voice;
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices');
    }
  },

  actions: {

    saveVoice(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated "' + model.get('description') + '" voice.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyVoice(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Deleted "' + model.get('description') + '" voice.');
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
