/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Resolver from '../../resolver';
import config from '../../config/environment';

const resolver = Resolver.create();

resolver.namespace = {
  modulePrefix: config.modulePrefix,
  podModulePrefix: config.podModulePrefix
};

export default resolver;
