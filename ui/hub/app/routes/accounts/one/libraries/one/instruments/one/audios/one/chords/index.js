// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let self = this;
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chords = this.store.query('audio-chord', {audioId: audio.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      audio: audio,
      chords: chords,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.audio.get('name') + ' ' + 'Chords',
      entity: {
        name: 'Voice',
        id: model.audio.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editChord(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords.one', model);
    },
  }

});
