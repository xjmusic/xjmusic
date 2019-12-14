// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

/* jshint node: true */

/* eslint-env node */
'use strict';

module.exports = function (environment) {
  let ENV = {
    baseURL: '/mk1/',
    rootURL: '/mk1/',
    modulePrefix: 'hub-mk1-ui',
    environment: environment,
    locationType: 'auto',
    'ember-cli-string-helpers': {
      only: ['lowercase'],
    },
    EmberENV: {
      FEATURES: {
        // Here you can enable experimental features on an ember canary build
        // e.g. 'with-controller': true
        "glimmer-custom-component-manager": true
      },
      EXTEND_PROTOTYPES: {
        Date: false,
      }
    },
    // flashMessageDefaults: {
    //   timeout: 6000,
    //   extendedTimeout: 6000
    // },

    APP: {
      // Here you can pass flags/options to your application instance
      // when it is ofd
    },
  };

  if (environment === 'development') {
    ENV.APP.LOG_RESOLVER = false;
    ENV.APP.LOG_ACTIVE_GENERATION = false;
    ENV.APP.LOG_TRANSITIONS = false;
    ENV.APP.LOG_TRANSITIONS_INTERNAL = false;
    ENV.APP.LOG_VIEW_LOOKUPS = false;
  }

  if (environment === 'test') {
    ENV.baseURL = '/'; // Testem requires deprecated "baseURL"
    ENV.locationType = 'none'; // Testem prefers locationType none

    // keep test console output quieter
    ENV.APP.LOG_ACTIVE_GENERATION = false;
    ENV.APP.LOG_VIEW_LOOKUPS = false;

    ENV.APP.rootElement = '#ember-testing';
    ENV.APP.autoboot = false;
  }

  if (environment === 'production') {
    ENV.googleAnalytics = {
      webPropertyId: 'UA-102762643-1'
    };
  }

  return ENV;
};
