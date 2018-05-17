// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {hash} from 'rsvp';

import {get} from '@ember/object';
import {inject as service} from '@ember/service';
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
    let chords = this.store.query('pattern-chord', {patternId: pattern.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      pattern: pattern,
      chords: chords,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editChord(model) {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.chords.one', model);
    },
  }

});
