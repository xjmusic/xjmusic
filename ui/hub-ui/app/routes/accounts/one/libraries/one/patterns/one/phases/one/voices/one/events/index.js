// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { hash } from 'rsvp';

import { get } from '@ember/object';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let self = this;
    let voice = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one');
    let events = this.store.query('voice-event', {voiceId: voice.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      voice: voice,
      events: events,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editEvent(event) {
      this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.voices.one.events.one', event);
    },
  }

});
