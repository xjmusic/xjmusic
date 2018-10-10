//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {get} from '@ember/object';

import {hash} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*} hash
   */
  model() {
    let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
    return hash({
      pattern: pattern,
      memeToAdd: null,
      patternMemes: this.store.query('pattern-meme', {patternId: pattern.id}),
    }, 'pattern, pattern memes, meme to add to pattern');
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    destroyPatternMeme(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Meme from Pattern.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addMemeToPattern(model) {
      let self = this;
      let patternMeme = this.store.createRecord('pattern-meme', {
        pattern: model.pattern,
        name: model.memeToAdd,
      });
      patternMeme.save().then(
        () => {
          get(self, 'display').success('Added ' + patternMeme.get('name') + ' to ' + model.pattern.get('name') + '.');
          self.send("sessionChanged");
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },
  }

});
