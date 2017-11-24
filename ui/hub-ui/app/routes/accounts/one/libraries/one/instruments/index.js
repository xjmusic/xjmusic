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
    let library = this.modelFor('accounts.one.libraries.one');
    let instruments = this.store.query('instrument', {libraryId: library.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      library: library,
      instruments: instruments,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editInstrument(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one', model);
    },

  }

});
