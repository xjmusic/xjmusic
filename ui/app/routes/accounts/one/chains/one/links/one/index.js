// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let chain = this.modelFor('accounts.one.chains.one');
    let link = this.modelFor('accounts.one.chains.one.links.one');
    return Ember.RSVP.hash({
      chain: chain,
      link: link,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.link.getTitle(),
      state: model.link.get('state'),
      detail: {
        total: model.link.get('total') + ' ' + 'Beats'
      },
      entity: {
        name: 'Link',
        id: model.link.get('id')
      }
    });
  }

});
