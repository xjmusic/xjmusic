// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {inject as service} from '@ember/service';
import {computed} from '@ember/object';

import Component from '@ember/component';

/**
 * Displays Segment Choices
 */
const SegmentChoicesComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Choices in order of type
   */
  orderedChoices: computed('model', function () {
    let sorted = [];
    this.get('model').get('segmentChoices').forEach(choice => {
      sorted.push(choice);
    });
    sorted.sort((a, b) => {
      if (a.type > b.type) return 1;
      if (a.type < b.type) return -1;
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
 *   {{segment-choices mySegmentModel}}
 */
SegmentChoicesComponent.reopenClass({
  positionalParams: ['model']
});

export default SegmentChoicesComponent;
