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
    let pattern = this.modelFor('accounts.one.libraries.one.patterns.one');
    let voices = this.store.query('voice', {patternId: pattern.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      pattern: pattern,
      voices: voices,
    });
  },

  /**
   * Route Action
   */
  actions: {

    editVoice(model) {
      let pattern = model.get('pattern');
      let library = pattern.get('library');
      let account = pattern.get('account');
      this.transitionTo('accounts.one.libraries.one.patterns.one.voices.one', account, library, pattern, model);
    },
  }

});
