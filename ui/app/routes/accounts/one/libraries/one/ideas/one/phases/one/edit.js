// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  auth: Ember.inject.service(),

  display: Ember.inject.service(),

  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
      let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
      phase.set('idea', idea);
      return phase;
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one');
    }
  },

  actions: {

    savePhase(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated phase ' + model.get('name') + '.');
        this.transitionTo('accounts.one.libraries.one.ideas.one.phases', model.get('idea'));
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyPhase(model) {
      let confirmation = confirm("Are you sure? If there are Phases or Instruments belonging to this Phase, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord().then(() => {
          Ember.get(this, 'display').success('Deleted phase ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.ideas.one.phases');
        }).catch((error) => {
          Ember.get(this, 'display').error(error);
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
