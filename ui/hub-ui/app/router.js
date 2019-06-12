//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import EmberRouter from '@ember/routing/router';
import config from "./config/environment";
import googlePageView from "./mixins/google-pageview";

const Router = EmberRouter.extend(googlePageView, {
  location: config.locationType
});

Router.map(function () {

  this.route('users', {path: '/u'}, function () {
    this.route('one', {path: '/:user_id'});
  });

  this.route('accounts', {path: '/a'}, function () {
    this.route('new');
    this.route('one', {path: '/:account_id'}, function () {
      this.route('edit');
      this.route('destroy');
      this.route('users');

      this.route('libraries', {path: '/lib'}, function () {
        this.route('new');
        this.route('one', {path: '/:library_id'}, function () {
          this.route('edit');
          this.route('destroy');

          this.route('programs', function () {
            this.route('editor', {path: '/:program_id'});
          });

          this.route('instruments', function () {
            this.route('editor', {path: '/:instrument_id'});
          });

          this.route('digest');
        });
      });

      this.route('chains', function () {
        this.route('new');
        this.route('one', {path: '/:chain_id'}, function () {
          this.route('edit');
          this.route('destroy');
          this.route('configs');
          this.route('bindings');

          this.route('segments', function () {
              this.route('one', {path: '/:segment_id'}, function () {
              });
            }
          );
        });
      });
    });
  });

  this.route('login');
  this.route('logout');
  this.route('unauthorized');

  this.route('platform', function () {
    this.route('messages', function () {
      this.route('new');
    });
    this.route('works');
  });

  this.route('go', function () {
      this.route('program', {path: '/program/:program_id'});
      this.route('instrument', {path: '/instrument/:instrument_id'});
    }
  );
});

export default Router;
