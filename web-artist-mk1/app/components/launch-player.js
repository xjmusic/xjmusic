/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Component from '@ember/component';
import {computed} from '@ember/object';
import {inject as service} from '@ember/service';

const LaunchPlayerComponent = Component.extend({
  config: service(),
  playerBaseUrl: null,

  didRender() {
    let self = this;
    this.config.getConfig().then(
      () => {
        self.set('playerBaseUrl', self.config.playerBaseUrl);
      },
      (error) => {
        console.error('Failed to load config', error);
      }
    );
  },
  playerUrl: computed('embedKey', 'playerBaseUrl', function () {
    return `${this.playerBaseUrl}#play=${this.embedKey}`;
  }),
});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{launch-player <embedKey>}}
 */
LaunchPlayerComponent.reopenClass({
  positionalParams: ['embedKey']
});

export default LaunchPlayerComponent;

