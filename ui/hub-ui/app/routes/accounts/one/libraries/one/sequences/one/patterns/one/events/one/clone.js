//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';
import { hash } from 'rsvp';

import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*|DS.Model|Promise}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
      let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
      let originalEvent = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one.events.one');
      let event = this.store.createRecord('pattern-event', {
        voice: originalEvent.get('voice'),
        pattern: originalEvent.get('pattern'),
        duration: originalEvent.get('duration'),
        inflection: originalEvent.get('inflection'),
        note: originalEvent.get('note'),
        position: originalEvent.get('position'),
        tonality: originalEvent.get('tonality'),
        velocity: originalEvent.get('velocity'),
      });
      event.set('pattern', pattern);
      let self = this;
      let voices = this.store.query('voice', {sequenceId: sequence.get('id')})
        .catch((error) => {
          get(self, 'display').error(error);
          self.transitionTo('');
        });
      return hash({
        event: event,
        voices: voices,
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.events');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    cloneEvent(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Cloned event "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          history.back();
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = get(this, 'controller.model.event');
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
