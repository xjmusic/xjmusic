// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*|DS.Model|Promise}
   */
  model: function () {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      return this.store.createRecord('phase-chord', {
        phase: this.modelFor('accounts.one.libraries.one.ideas.one.phases.one')
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords');
    }
  },

  /**
   * Headline
   */
  afterModel() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    Ember.set(this, 'routeHeadline', {
      title: 'New Chord',
      entity: {
        name: 'Phase',
        id: phase.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createChord(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Created chord ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords');
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
