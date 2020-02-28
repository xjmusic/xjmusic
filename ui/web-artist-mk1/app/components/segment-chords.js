/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {inject as service} from '@ember/service';
import {computed} from '@ember/object';

import Component from '@ember/component';

/**
 * Displays Segment Chords
 */
const SegmentChordsComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Chords in order of position
   */
  orderedChords: computed('model', function () {
    let sorted = [];
    this.get('model').get('segmentChords').forEach(chord=> {
      sorted.push(chord);
    });
    sorted.sort((a, b) => {
      if (a.position > b.position) return 1;
      if (a.position < b.position) return -1;
      return 0;
    });
    return sorted;
  }),

  /**
   * Component Actions
   */
  actions: {},

});

/**
 * Usage (e.g, in Handlebars, where segment model is "mySegmentModel"):
 *
 *   {{segment-chords mySegmentModel}}
 */
SegmentChordsComponent.reopenClass({
  positionalParams: ['model']
});

export default SegmentChordsComponent;
