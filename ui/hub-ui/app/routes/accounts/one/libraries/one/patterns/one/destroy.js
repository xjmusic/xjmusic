// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
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
    return this.modelFor('accounts.one.libraries.one.patterns.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let confirmation = confirm("Are you sure? If there are Phases or Memes belonging to this Pattern, destruction will fail anyway.");
      let library = model.get('library');
      let account = library.get('account');

      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(this, 'display').success('Destroyed pattern ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.patterns', account, library);
          },
          (error) => {
            get(this, 'display').error(error);
            model.rollbackAttributes();
          });
      }
    },

  }

});
