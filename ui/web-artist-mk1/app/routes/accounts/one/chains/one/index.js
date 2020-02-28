/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {hash} from 'rsvp';
import Route from '@ember/routing/route';

export default Route.extend({

  /**
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let account = this.modelFor('accounts.one');
    let chain = this.modelFor('accounts.one.chains.one');
    return hash({
      account: account,
      chain: chain,
    }, 'account, chain');
  },

});
