// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { hash } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let phase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
    return hash({
      phase: phase,
      memeToAdd: null,
      phaseMemes: this.store.query('phase-meme', {phaseId: phase.id}),
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    destroyPhaseMeme(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Meme from Phase.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addMemeToPhase(model) {
      let phaseMeme = this.store.createRecord('phase-meme', {
        phase: model.phase,
        name: model.memeToAdd,
      });
      phaseMeme.save().then(
        () => {
          get(this, 'display').success('Added ' + phaseMeme.get('name') + ' to ' + model.pattern.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },
  }

});
