// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  auth: Ember.inject.service(),

  display: Ember.inject.service(),

  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
      let chord = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.chords.one');
      chord.set('phase', phase);
      return chord;
    } else {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords');
    }
  },

  actions: {

    saveChord(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated chord ' + model.get('name') + '.');
        this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords', model.get('phase'));
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyChord(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Deleted chord ' + model.get('name') + '.');
        this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
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
