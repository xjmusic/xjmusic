// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    return Ember.RSVP.hash({
      phase: phase,
      memeToAdd: null,
      phaseMemes: this.store.query('phase-meme', {phaseId: phase.id}),
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.phase.getTitle() + ' ' + 'Memes',
      entity: {
        name: 'Phase',
        id: model.phase.get('id')
      }
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
          Ember.get(this, 'display').success('Removed Meme from Phase.');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    addMemeToPhase(model) {
      let phaseMeme = this.store.createRecord('phase-meme', {
        phase: model.phase,
        name: model.memeToAdd,
      });
      phaseMeme.save().then(
        () => {
          Ember.get(this, 'display').success('Added ' + phaseMeme.get('name') + ' to ' + model.phase.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },
  }

});
