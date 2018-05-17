// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Component from '@ember/component';

/**
 * Displays Segment Choices
 */
const SegmentChoicesComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Component Actions
   */
  actions: {

  },

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
