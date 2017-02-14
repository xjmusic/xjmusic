// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    return Ember.RSVP.hash({
      phase: phase,
      memeToAdd: null,
      phaseMemes: this.store.query('phase-meme', { phaseId: phase.id }),
    });
  },

  actions: {

    sessionChanged: function() {
      this.refresh();
    },

    destroyPhaseMeme(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Removed Meme from Phase.');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    addMemeToPhase(model) {
      let phaseMeme = this.store.createRecord('phase-meme', {
        phase: model.phase,
        name: model.memeToAdd,
      });
      phaseMeme.save().then(() => {
        Ember.get(this, 'display').success('Added ' + phaseMeme.get('name') + ' to ' + model.phase.get('name') + '.');
        this.send("sessionChanged");
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

  }


});
