// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  // Inject auth service
  auth: service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model() {
    return this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.events.one');
  },

  afterModel() {
    let self = this;
    self.model().destroyRecord({}).then(
      () => {
        get(self, 'display').success('Destroyed event ' + self.model().get('inflection') + '.');
        history.back();
      },
      (error) => {
        get(self, 'display').error(error);
        self.model().rollbackAttributes();
        history.back();
      });
  },

});
