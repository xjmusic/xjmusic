// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function () {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let events = this.store.query('audio-event', {audioId: audio.get('id')})
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('');
      });
    return Ember.RSVP.hash({
      audio: audio,
      events: events,
    });
  },

  actions: {

    editEvent(event) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.events.one', event);
    },

  }
});
