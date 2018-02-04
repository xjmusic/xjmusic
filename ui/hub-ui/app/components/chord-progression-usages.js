// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
// import {get, set} from '@ember/object';
import {inject as service} from '@ember/service';

/**
 * Displays the digest-performing and result-viewing U.I.
 */
const ChordProgressionUsagesComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   Component will render
   */
  willRender() {

  },


});

/**
 * Example Usage (e.g, in Handlebars):
 *
 *   {{chord-progression-usages chordProgression}}
 */
ChordProgressionUsagesComponent.reopenClass({
  positionalParams: ['model']
});

export default ChordProgressionUsagesComponent;
