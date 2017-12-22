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
    return this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
  },

  /**
   * Route Actions
   */
  actions: {

    destroy(model) {
      let pattern = model.get('pattern');
      let library = pattern.get('library');
      let account = library.get('account');
      let confirmation = confirm("Are you sure? If there are Phases or Instruments belonging to this Phase, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(this, 'display').success('Deleted phase ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries.one.patterns.one.phases', account, library, pattern);
          },
          (error) => {
            get(this, 'display').error(error);
            model.rollbackAttributes();
          });
      }
    },

  }

});
