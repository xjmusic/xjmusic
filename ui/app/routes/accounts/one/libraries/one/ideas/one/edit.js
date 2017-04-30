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
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Edit ' + model.get('name'),
      entity: {
        name: 'Idea',
        id: model.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    saveIdea(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated idea ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyIdea(model) {
      let confirmation = confirm("Are you sure? If there are Ideas or Instruments belonging to this Idea, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            Ember.get(this, 'display').success('Deleted idea ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.ideas');
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
