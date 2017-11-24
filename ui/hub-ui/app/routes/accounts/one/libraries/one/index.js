// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.

import { hash } from 'rsvp';
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
    let account = this.modelFor('accounts.one');
    let library = this.modelFor('accounts.one.libraries.one');
    return hash({
      account: account,
      library: library,
    });
  },

});
