//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
import {get, set} from '@ember/object';
import {inject as service} from '@ember/service';

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
    if (typeof this.types === "string") {
      set(this, "types", this.types.split("|"));
    }
  },

  actions: { // begin

    /**
     Action: Do Digest
     * @param type should be proper digestType like "DigestChordMarkov"
     * @param name to digest
     */
    doDigest(type, name) {
      let self = this;
      // FUTURE find('.active').removeClass('active');
      // FUTURE find('.' + type).addClass('active');
      // FUTURE addClass('loading');
      // FUTURE removeClass('done failed');
      set(self, 'result', {});
      // Future: to digests for entities beyond only a single library
      fetch(`/api/1/digest?type=${type}&libraryId=${get(self, 'model').get('id')}`)
        .then(
          (data) => {
            if (data.hasOwnProperty('digest')) {
              set(self, 'result', data['digest']);
              get(self, 'display').success('Did ' + name + ' digest.');
              // FUTURE addClass('done');
              // FUTURE removeClass('loading');

            } else {
              // FUTURE addClass('failed');
              // FUTURE removeClass('loading');
              get(self, 'display').error('Failed; ' + name + ' digest was empty.');
            }

          },
          (error) => {
            // FUTURE addClass('failed');
            // FUTURE removeClass('loading');
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
