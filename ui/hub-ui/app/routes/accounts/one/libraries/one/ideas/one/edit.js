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
          reject('Could not instantiate Idea model', error);
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
      let library = this.modelFor('accounts.one.libraries.one');
      let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
      idea.set('library', library);
      return idea;
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveIdea(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated idea ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyIdea(model) {
      let confirmation = confirm("Are you sure? If there are Ideas or Instruments belonging to this Idea, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(this, 'display').success('Deleted idea ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.ideas');
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
