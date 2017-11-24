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
        content: loadContentFromHTML('app/templates/components/global-footer.hbs')
      },
      'marketing': {
        content: loadContentFromHTML('app/templates/static/marketing.hbs')
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
      return 'https://hub.xj.io/';
    case 'development':
      return 'http://hub.xj.dev/';
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
      // Wavesurfer
      app.import('node_modules/wavesurfer.js/dist/wavesurfer.min.js');

      // Popper
      app.import('node_modules/popper.js/dist/umd/popper.min.js');

      // Bootstrap (minified)
      app.import('node_modules/bootstrap/dist/css/bootstrap-reboot.min.css');
      app.import('node_modules/bootstrap/dist/css/bootstrap-grid.min.css');
      app.import('node_modules/bootstrap/dist/css/bootstrap.min.css');
      app.import('node_modules/bootstrap/dist/js/bootstrap.min.js');
      break;

    default:

      // Wavesurfer
      app.import('node_modules/wavesurfer.js/dist/wavesurfer.js');

      // Popper
      app.import('node_modules/popper.js/dist/umd/popper.js');

      // Bootstrap
      app.import('node_modules/bootstrap/dist/css/bootstrap-reboot.css');
      app.import('node_modules/bootstrap/dist/css/bootstrap-grid.css');
      app.import('node_modules/bootstrap/dist/css/bootstrap.css');
      app.import('node_modules/bootstrap/dist/js/bootstrap.js');
      break;
  }

  // Runs in any environment
  app.import('vendor/shims/wavesurfer.js', {
    exports: {
      'wavesurfer': [ 'default' ]
    }
  });
}

/**
 [#314] Placeholder content index.html page should not have handlebars blocks
 * @param path
 */
function loadContentFromHTML(path) {
  let content = fs.readFileSync(path, 'utf8');
  if (content && content.length > 0) {
    return content

    // replace year with this year
      .replace(/{{year}}/g, new Date().getFullYear().toString())

      // strip handlebars blocks
      .replace(/{{[^}]+}}/g, '')

      // strip html comments handlebars blocks
      .replace(/<!--[\s\S]*?-->/g, '');

  } else {
    return '';
  }
}
