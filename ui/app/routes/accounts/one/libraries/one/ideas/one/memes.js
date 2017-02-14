// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    return Ember.RSVP.hash({
      idea: idea,
      memeToAdd: null,
      ideaMemes: this.store.query('idea-meme', { ideaId: idea.id }),
    });
  },

  actions: {

    sessionChanged: function() {
      this.refresh();
    },

    destroyIdeaMeme(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Removed Meme from Idea.');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    addMemeToIdea(model) {
      let ideaMeme = this.store.createRecord('idea-meme', {
        idea: model.idea,
        name: model.memeToAdd,
      });
      ideaMeme.save().then(() => {
        Ember.get(this, 'display').success('Added ' + ideaMeme.get('name') + ' to ' + model.idea.get('name') + '.');
        this.send("sessionChanged");
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

  }


});
