// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get, set } from '@ember/object';

import Route from '@ember/routing/route';

export default Route.extend({

  model(params) {
    let self = this;
    return this.store.findRecord('voice', params.voice_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.patterns.one.voices');
      });
  },

  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.get("description")
    });
  }

});
