// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
// import {get, set} from '@ember/object';
import {inject as service} from '@ember/service';

/**
 * Displays the analysis-performing and result-viewing U.I.
 */
const ChordSequenceComponent = Component.extend({

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
 *   {{chord-sequence chordSequence}}
 */
ChordSequenceComponent.reopenClass({
  positionalParams: ['model']
});

export default ChordSequenceComponent;
