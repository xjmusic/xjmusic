// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Application from '@ember/application';

import Resolver from "./resolver";
import loadInitializers from "ember-load-initializers";
import config from "./config/environment";

let App;

App = Application.extend({
  modulePrefix: config.modulePrefix,
  podModulePrefix: config.podModulePrefix,
  Resolver
});

loadInitializers(App, config.modulePrefix);

export default App;
