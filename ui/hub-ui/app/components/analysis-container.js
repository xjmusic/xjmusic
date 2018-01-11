// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
import $ from 'jquery';
import {get, set} from '@ember/object';
import {inject as service} from '@ember/service';

/**
 * Displays the analysis-performing and result-viewing U.I.
 */
const AnalysisContainerComponent = Component.extend({

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
     Action: Do Analysis
     * @param type
     */
    doAnalysis(type) {
      let self = this;
      let div = self.$('.analysis');
      div.find('.active').removeClass('active');
      div.find('.' + type).addClass('active');
      div.addClass('loading');
      div.removeClass('done failed');
      set(self, 'result', {});
      $.ajax({
        url: '/api/1/analysis',
        type: 'get',
        data: {
          type: type,
          entityId: get(self, 'model').get('id')
        }
      }).then(
        (data) => {
          if (data.hasOwnProperty('analysis')) {
            set(self, 'result', data['analysis']);
            get(self, 'display').success('Did ' + type + ' analysis.');
            div.addClass('done');
            div.removeClass('loading');

          } else {
            div.addClass('failed');
            div.removeClass('loading');
            get(self, 'display').error('Failed; ' + type + ' analysis was empty.');
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
 *   {{analysis-container model 'LibraryMeme|LibraryChord'}}
 */
AnalysisContainerComponent.reopenClass({
  positionalParams: ['model', 'types']
});

export default AnalysisContainerComponent;
