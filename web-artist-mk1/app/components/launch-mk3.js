/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Component from '@ember/component';

const LaunchMk3Component = Component.extend({});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{launch-mk3 <mk3path>}}
 */
LaunchMk3Component.reopenClass({
  positionalParams: ['mk3url']
});

export default LaunchMk3Component;

