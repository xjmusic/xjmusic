// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    return Ember.RSVP.hash({
      idea: idea,
      memeToAdd: null,
      ideaMemes: this.store.query('idea-meme', {ideaId: idea.id}),
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.idea.get('name') + ' ' + 'Memes',
      entity: {
        name: 'Idea',
        id: model.idea.get('id')
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

    destroyIdeaMeme(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Removed Meme from Idea.');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    addMemeToIdea(model) {
      let ideaMeme = this.store.createRecord('idea-meme', {
        idea: model.idea,
        name: model.memeToAdd,
      });
      ideaMeme.save().then(
        () => {
          Ember.get(this, 'display').success('Added ' + ideaMeme.get('name') + ' to ' + model.idea.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

  }


});
