// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
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
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    return hash({
      idea: idea,
      memeToAdd: null,
      ideaMemes: this.store.query('idea-meme', {ideaId: idea.id}),
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
          get(this, 'display').success('Removed Meme from Idea.');
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    addMemeToIdea(model) {
      let ideaMeme = this.store.createRecord('idea-meme', {
        idea: model.idea,
        name: model.memeToAdd,
      });
      ideaMeme.save().then(
        () => {
          get(this, 'display').success('Added ' + ideaMeme.get('name') + ' to ' + model.idea.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  }


});
