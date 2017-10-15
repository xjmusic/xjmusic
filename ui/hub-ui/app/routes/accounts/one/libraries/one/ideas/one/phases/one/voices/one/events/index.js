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
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    let events = this.store.query('voice-event', {voiceId: voice.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      voice: voice,
      events: events,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.voice.get('description') + ' ' + 'Events',
      entity: {
        name: 'Voice',
        id: model.voice.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editEvent(event) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events.one', event);
    },
  }

});
