//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
    let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
    let events = this.store.query('pattern-event', {patternId: pattern.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      pattern: pattern,
      events: events,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editEvent(event) {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.events.one', event);
    },
  }

});
