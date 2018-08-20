//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
import $ from 'jquery';
import { get, set } from '@ember/object';
import { inject as service } from '@ember/service';

/**
 * Displays the digest-performing and result-viewing U.I.
 */
const DigestContainerComponent = Component.extend({

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
     Action: Do Digest
     * @param type should be proper digestType like "DigestChordMarkov"
     */
    doDigest(type, name) {
      let self = this;
      let div = self.$('.digest');
      div.find('.active').removeClass('active');
      div.find('.' + type).addClass('active');
      div.addClass('loading');
      div.removeClass('done failed');
      set(self, 'result', {});
      $.ajax({
        url: '/api/1/digest',
        type: 'get',
        data: {
          type: type,
          libraryId: get(self, 'model').get('id') // Future: to digests for entities beyond only a single library
        }
      }).then(
        (data) => {
          if (data.hasOwnProperty('digest')) {
            set(self, 'result', data['digest']);
            get(self, 'display').success('Did ' + name + ' digest.');
            div.addClass('done');
            div.removeClass('loading');

          } else {
            div.addClass('failed');
            div.removeClass('loading');
            get(self, 'display').error('Failed; ' + name + ' digest was empty.');
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
 *   {{digest-container model 'Digest Memes|Digest Chords'}}
 */
DigestContainerComponent.reopenClass({
  positionalParams: ['model', 'types']
});

export default DigestContainerComponent;
