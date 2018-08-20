//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
      let chord = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one.chords.one');
      chord.set('pattern', pattern);
      return chord;
    } else {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.chords');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveChord(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated chord ' + model.get('name') + '.');
          history.back();
        },
        (error) => {
          get(this, 'display').error(error);
        });
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
