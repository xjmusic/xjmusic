/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get, set} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  model(params) {
    let self = this;
    return this.store.findRecord('audio_chord', params.chord_id)
      .catch((error) => {
        get(self, 'display').error(error);
        history.back();
      });
  },

  afterModel(model) {
    set(this, 'breadCrumb', {
      title: model.get("name") + '@' + model.get("position")
    });
  }

});
