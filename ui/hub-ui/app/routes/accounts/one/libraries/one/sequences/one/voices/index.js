// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    let voices = this.store.query('voice', {sequenceId: sequence.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      sequence: sequence,
      voices: voices,
    });
  },

  /**
   * Route Action
   */
  actions: {

    editVoice(model) {
      let sequence = model.get('sequence');
      let library = sequence.get('library');
      let account = sequence.get('account');
      this.transitionTo('accounts.one.libraries.one.sequences.one.voices.one', account, library, sequence, model);
    },
  }

});
