// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Component from '@ember/component';

/**
 * Displays Link Memes
 */
const LinkMemesComponent = Component.extend({

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
 *   {{link-memes myLinkModel}}
 */
LinkMemesComponent.reopenClass({
  positionalParams: ['model']
});

export default LinkMemesComponent;
