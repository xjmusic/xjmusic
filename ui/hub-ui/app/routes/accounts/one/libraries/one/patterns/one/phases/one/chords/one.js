// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get, set } from '@ember/object';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  model(params) {
    let self = this;
    return this.store.findRecord('phase-chord', params.chord_id)
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.chords');
      });
  },

  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.getTitle()
    });
  }

});
