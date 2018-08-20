//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

module.exports = {
  root: true,
  parserOptions: {
    ecmaVersion: 2017,
    sourceType: 'module'
  },
  extends: 'eslint:recommended',
  env: {
    browser: true
  },
  rules: {
    'no-console': 0
  }
};
