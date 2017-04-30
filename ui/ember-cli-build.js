/* jshint node:true */
/* global require, module */
let EmberApp = require('ember-cli/lib/broccoli/ember-app');

module.exports = function (defaults) {
  let app = getApp(defaults, process.env.EMBER_ENV);

  // import bootstrap
  importDependencies(app);

  // Use `app.import` to add additional libraries to the generated
  // output files.
  //
  // If you need to use different assets in different
  // environments, specify an object as the first parameter. That
  // object's keys should be the environment name and the values
  // should be the asset to use in that environment.
  //
  // If the library that you are including contains AMD or ES6
  // modules that you would like to import into your application
  // please specify an object with the list of modules as keys
  // along with the exports of each module as its value.

  return app.toTree();
};

/**
 * Get the default application for the environment
 * @param defaults
 * @param environment
 * @returns {EmberApp}
 */
function getApp(defaults, environment) {
  switch (environment) {
    case "production":
      return new EmberApp(defaults, {
        fingerprint: {
          enabled: true
        },
      });
    default:
      return new EmberApp(defaults, {
        fingerprint: {
          enabled: false
        },
      });
  }
}

/**
 * Import dependencies
 *
 * @param {EmberApp} app
 * @param environment
 */
function importDependencies(app, environment) {
  switch (environment) {

    case "production":
      // Tether
      app.import('bower_components/tether/dist/css/tether.min.css');
      app.import('bower_components/tether/dist/js/tether.min.js');

      // Bootstrap (minified)
      app.import('bower_components/bootstrap/dist/css/bootstrap-reboot.min.css');
      app.import('bower_components/bootstrap/dist/css/bootstrap-grid.min.css');
      app.import('bower_components/bootstrap/dist/css/bootstrap.min.css');
      app.import('bower_components/bootstrap/dist/js/bootstrap.min.js');
      break;

    default:
      // Tether
      app.import('bower_components/tether/dist/css/tether.css');
      app.import('bower_components/tether/dist/js/tether.js');

      // Bootstrap (alpha)
      app.import('bower_components/bootstrap/dist/css/bootstrap-reboot.css');
      app.import('bower_components/bootstrap/dist/css/bootstrap-grid.css');
      app.import('bower_components/bootstrap/dist/css/bootstrap.css');
      app.import('bower_components/bootstrap/dist/js/bootstrap.js');
      break;
  }
}
