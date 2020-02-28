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
    let chain = this.modelFor('accounts.one.chains.one');
    let segment = this.modelFor('accounts.one.chains.one.segments.one');
    return hash({
      chain: chain,
      segment: segment,
    }, 'chain, segment');
  },

});
