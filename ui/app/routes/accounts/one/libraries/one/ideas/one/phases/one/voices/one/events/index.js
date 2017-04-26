// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function () {
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    let events = this.store.query('voice-event', {voiceId: voice.get('id')})
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('');
      });
    return Ember.RSVP.hash({
      voice: voice,
      events: events,
    });
  },

  actions: {

    editEvent(event) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events.one', event);
    },

  }
});
