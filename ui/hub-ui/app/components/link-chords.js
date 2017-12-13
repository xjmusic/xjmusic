// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Component from '@ember/component';

/**
 * Displays Link Chords
 */
const LinkChordsComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Component Actions
   */
  actions: {

  },

});

/**
 * Usage (e.g, in Handlebars, where link model is "myLinkModel"):
 *
 *   {{link-chords myLinkModel}}
 */
LinkChordsComponent.reopenClass({
  positionalParams: ['model']
});

export default LinkChordsComponent;
