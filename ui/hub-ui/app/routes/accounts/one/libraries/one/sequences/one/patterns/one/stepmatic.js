//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';
import $ from 'jquery';

/**
 [#159669804] Artist wants a step sequencer in order to compose rhythm patterns in a familiar way.
 */
export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      return this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
    } else {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns');
    }
  },

  /**
   * Route actions
   */
  actions: {

    willTransition(transition) {
      if ($('.pattern-stepmatic.dirty').length) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (!confirmation) {
          transition.abort();
        }
      }
    }

  }

});
