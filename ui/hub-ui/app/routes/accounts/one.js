// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import {get, set} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject auth service
  auth: service(),

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('account', params.account_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts');
      });
  },

  /**
   * Route Breadcrumb and Headline
   * @param model
   */
  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
