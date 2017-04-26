// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function () {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chords = this.store.query('audio-chord', {audioId: audio.get('id')})
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('');
      });
    return Ember.RSVP.hash({
      audio: audio,
      chords: chords,
    });
  },

  actions: {

    editChord(chord) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords.one', chord);
    },

  }
});
