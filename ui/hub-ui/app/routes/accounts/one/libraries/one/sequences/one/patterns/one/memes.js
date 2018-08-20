//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
    let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
    return hash({
      pattern: pattern,
      memeToAdd: null,
      patternMemes: this.store.query('pattern-meme', {patternId: pattern.id}),
    });
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
      let patternMeme = this.store.createRecord('pattern-meme', {
        pattern: model.pattern,
        name: model.memeToAdd,
      });
      patternMeme.save().then(
        () => {
          get(this, 'display').success('Added ' + patternMeme.get('name') + ' to ' + model.sequence.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },
  }

});
