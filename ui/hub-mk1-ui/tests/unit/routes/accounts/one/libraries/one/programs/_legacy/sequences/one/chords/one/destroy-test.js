// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('Unit | Route | accounts/one/libraries/one/sequences/one/patterns/one/chords/one/destroy', function() {
  setupTest('route:accounts/one/libraries/one/sequences/one/patterns/one/chords/one/destroy', {
    // Specify the other units that are required for this test.
    needs: ['service:config','service:display','service:auth']
  });

  it('exists', function() {
    let route = this.subject();
    expect(route).to.be.ok;
  });
});
