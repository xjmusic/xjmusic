// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
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
    return this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let self = this;
      let instrument = model.get('instrument');
      let library = instrument.get('library');
      let account = library.get('account');
      model.destroyRecord({}).then(
        () => {
          get(self, 'display').success('Destroyed audio "' + model.get('name') + '".');
          self.transitionTo('accounts.one.libraries.one.instruments.one.audios', account, library, instrument);
        },
        (error) => {
          get(self, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    cancel() {
      history.back();
    },

  }

});
