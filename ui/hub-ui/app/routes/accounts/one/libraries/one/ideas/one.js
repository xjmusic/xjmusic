// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get, set } from '@ember/object';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  model(params) {
    let self = this;
    return this.store.findRecord('idea', params.idea_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.ideas');
      });
  },

  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.get("name")
    });
  }

});
