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
   * @returns {*} hash
   */
  model: function () {
    let self = this;
    let library = this.modelFor('accounts.one.libraries.one');
    let sequences = this.store.query('sequence', {libraryId: library.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      library: library,
      sequences: sequences,
    }, 'library, sequences');
  },

  /**
   * Route Actions
   */
  actions: {

    editSequence(model) {
      this.transitionTo('accounts.one.libraries.one.sequences.one', model);
    },

  }

});
