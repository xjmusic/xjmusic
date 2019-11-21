//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

/*jshint node:true*/
/* eslint-env node */
module.exports = {
  "framework": "qunit",
  "test_page": "tests/index.html?hidepassed",
  "disable_watching": true,
  "launch_in_ci": [
    "PhantomJS"
  ],
  "launch_in_dev": [
    "PhantomJS"
  ]
};
