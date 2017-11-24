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
          get(this, 'display').success('Updated phase ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyPhase(model) {
      let confirmation = confirm("Are you sure? If there are Phases or Instruments belonging to this Phase, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(this, 'display').success('Deleted phase ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.ideas.one.phases');
          },
          (error) => {
            get(this, 'display').error(error);
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
