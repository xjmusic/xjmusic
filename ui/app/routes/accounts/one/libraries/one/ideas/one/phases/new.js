// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*|Promise|DS.Model}
   */
  model: function () {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      return this.store.createRecord('phase', {
        idea: this.modelFor('accounts.one.libraries.one.ideas.one')
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases');
    }
  },

  /**
   * Headline
   */
  afterModel() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    Ember.set(this, 'routeHeadline', {
      title: 'New Phase',
      entity: {
        name: 'Idea',
        id: idea.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createPhase(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Created phase ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one', model);
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
