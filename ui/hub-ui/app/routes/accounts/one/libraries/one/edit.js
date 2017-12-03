// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model() {
    let account = this.modelFor('accounts.one');
    let library = this.modelFor('accounts.one.libraries.one');
    library.set('account', account);
    return library;
  },

  /**
   * Route Actions
   */
  actions: {

    saveLibrary(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated library ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one', model);
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyLibrary(model) {
      let confirmation = confirm("Are you sure? If there are Patterns or Instruments belonging to this Library, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            get(this, 'display').success('Deleted library ' + model.get('name') + '.');
            this.transitionTo('accounts.one.libraries');
          },
          (error) => {
            get(this, 'display').error(error);
          });
      }
    },

    willTransition(transition) {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    }

  }

});
