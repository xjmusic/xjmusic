// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let event = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.events.one');
    return Ember.RSVP.hash({
      audio: audio,
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
