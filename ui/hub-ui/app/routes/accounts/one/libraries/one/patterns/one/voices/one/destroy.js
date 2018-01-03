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
    return this.modelFor('accounts.one.libraries.one.patterns.one.voices.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let pattern = model.get('pattern');
      let library = pattern.get('library');
      let account = library.get('account');
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Destroyed "' + model.get('description') + '" voice.');
          this.transitionTo('accounts.one.libraries.one.patterns.one.voices', account, library, pattern);
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

  }

});
