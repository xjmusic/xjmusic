// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

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
          reject('Could not instantiate Voice model', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @param config
   * @returns {*}
   */
  resolvedModel() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
      let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
      phase.set('idea', idea);
      return phase;
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    savePhase(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated phase ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyPhase(model) {
      let confirmation = confirm("Are you sure? If there are Phases or Instruments belonging to this Phase, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            Ember.get(this, 'display').success('Deleted phase ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.ideas.one.phases');
          },
          (error) => {
            Ember.get(this, 'display').error(error);
          });
      }
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
