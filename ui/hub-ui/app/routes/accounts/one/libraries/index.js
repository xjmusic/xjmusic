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
    let account = this.modelFor('accounts.one');
    let libraries = this.store.query('library', {accountId: account.get('id')})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
    return hash({
      account: account,
      libraries: libraries,
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editLibrary(model) {
      this.transitionTo('accounts.one.libraries.one', model);
    },

  }

});
