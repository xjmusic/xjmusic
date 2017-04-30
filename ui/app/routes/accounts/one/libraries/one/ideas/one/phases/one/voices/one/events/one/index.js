// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    let event = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events.one');
    return Ember.RSVP.hash({
      voice: voice,
      event: event,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.event.getTitle(),
      entity: {
        name: 'Event',
        id: model.event.get('id')
      }
    });
  },

});
