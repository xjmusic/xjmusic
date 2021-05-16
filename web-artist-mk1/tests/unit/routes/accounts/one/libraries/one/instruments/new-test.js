/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('Unit | Route | accounts/one/libraries/one/instruments/new', function() {
  setupTest('route:accounts/one/libraries/one/instruments/new', {
    needs: ['service:auth','service:config','service:display']
  });

  it('exists', function() {
    let route = this.subject();
    expect(route).to.be.ok;
  });
});
