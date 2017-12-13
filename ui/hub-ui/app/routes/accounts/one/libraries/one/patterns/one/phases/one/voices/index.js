// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
    let phase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
    let voices = this.store.query('voice', {phaseId: phase.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      phase: phase,
      voices: voices,
    });
  },

  /**
   * Route Action
   */
  actions: {

    editVoice(model) {
      this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.voices.one', model);
    },
  }

});
