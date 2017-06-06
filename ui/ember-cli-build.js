/* jshint node:true */
/* global require, module */
let EmberApp = require('ember-cli/lib/broccoli/ember-app');
let fs = require('fs');
let metadata = require('./metadata');

module.exports = function (defaults) {
  let app = new EmberApp(defaults, optionsForEnvironment(process.env.EMBER_ENV));

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
 * @param environment
 * @returns {Object} options
 *
 * [#318] Static content is injected into index.html via {{content-for "static"}}
 */
function optionsForEnvironment(environment) {
  return {
    fingerprint: {
      enabled: isFingerprintEnabledForEnvironment(environment)
    },
    inlineContent: {
      'meta-title': {
        content: metadata.title,
      },
      'meta-description': {
        content: metadata.description,
      },
      'meta-image': {
        content: metadata.image,
      },
      'url': {
        content: urlForEnvironment(environment)
      },
      'footer': {
        content: stripHandlebars('app/templates/components/global-footer.hbs')
      },
      'marketing': {
        content: stripHandlebars('app/templates/static/marketing.hbs')
      }
    }
  };
}

/**
 URL for an environment
 * @param environment
 * @returns {String}
 */
function urlForEnvironment(environment) {
  switch (environment) {
    case 'production':
      return 'https://xj.io/';
    case 'development':
      return 'http://xj.dev/';
    default:
      return '/';
  }
}

/**
 Is fingerprint enabled for environment?
 * @param environment
 * @returns {Boolean}
 */
function isFingerprintEnabledForEnvironment(environment) {
  switch (environment) {
    case 'production':
      return true;
    case 'development':
      return false;
    default:
      return false;
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

/**
 [#314] Placeholder content index.html page should not have handlebars blocks
 * @param path
 */
function stripHandlebars(path) {
  let content = fs.readFileSync(path, 'utf8');
  if (content && content.length > 0) {
    return content
      .replace(/{{year}}/g, new Date().getFullYear().toString())
      .replace(/{{[^}]+}}/g, '');
  } else {
    return '';
  }
}
