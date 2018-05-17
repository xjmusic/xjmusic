// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get, set } from '@ember/object';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  model(params) {
    let self = this;
    return this.store.findRecord('pattern', params.pattern_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.sequences.one.patterns');
      });
  },

  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.getTitle()
    });
  }

});
