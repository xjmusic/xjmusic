// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
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
      return this.store.createRecord('audio-chord', {
        audio: this.modelFor('accounts.one.libraries.one.instruments.one.audios.one')
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords');
    }
  },

  /**
   * Headline
   */
  afterModel() {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    Ember.set(this, 'routeHeadline', {
      title: 'New Chord',
      entity: {
        name: 'Audio',
        id: audio.get('id')
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
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords');
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
