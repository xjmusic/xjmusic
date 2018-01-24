// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
import $ from 'jquery';
import {get, set} from '@ember/object';
import {inject as service} from '@ember/service';

/**
 * Displays the evaluation-performing and result-viewing U.I.
 */
const EvaluationContainerComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   Component will render
   */
  willRender() {
    if (typeof get(this, "types") === "string") {
      set(this, "types", get(this, "types").split("|"));
    }
  },

  actions: { // begin

    /**
     Action: Do Evaluation
     * @param type should be proper digestType like "DigestChordMarkov"
     */
    doEvaluation(type, name) {
      let self = this;
      let div = self.$('.evaluation');
      div.find('.active').removeClass('active');
      div.find('.' + type).addClass('active');
      div.addClass('loading');
      div.removeClass('done failed');
      set(self, 'result', {});
      $.ajax({
        url: '/api/1/evaluation',
        type: 'get',
        data: {
          type: type,
          libraryId: get(self, 'model').get('id') // Future: to evaluations for entities beyond only a single library
        }
      }).then(
        (data) => {
          if (data.hasOwnProperty('digest')) {
            set(self, 'result', data['digest']);
            get(self, 'display').success('Did ' + name + ' evaluation.');
            div.addClass('done');
            div.removeClass('loading');

          } else {
            div.addClass('failed');
            div.removeClass('loading');
            get(self, 'display').error('Failed; ' + name + ' evaluation was empty.');
          }

        },
        (error) => {
          div.addClass('failed');
          div.removeClass('loading');
          get(self, 'display').error(JSON.parse(error.responseText));
        });
    }


  } // actions end

});

/**
 * Example Usage (e.g, in Handlebars):
 *
 *   {{evaluation-container model 'Digest Memes|Digest Chords'}}
 */
EvaluationContainerComponent.reopenClass({
  positionalParams: ['model', 'types']
});

export default EvaluationContainerComponent;
