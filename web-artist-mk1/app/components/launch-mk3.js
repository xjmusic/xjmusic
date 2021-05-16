/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Component from '@ember/component';

const LaunchLabComponent = Component.extend({});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{launch-lab <labpath>}}
 */
LaunchLabComponent.reopenClass({
  positionalParams: ['labUrl']
});

export default LaunchLabComponent;

