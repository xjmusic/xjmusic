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
    let library = this.modelFor('accounts.one.libraries.one');
    let patterns = this.store.query('pattern', {libraryId: library.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      library: library,
      patterns: patterns,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editPattern(model) {
      this.transitionTo('accounts.one.libraries.one.patterns.one', model);
    },

  }

});
