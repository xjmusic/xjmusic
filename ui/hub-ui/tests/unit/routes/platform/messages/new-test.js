//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('Unit | Route | platform/messages/new', function() {
  setupTest('route:platform/messages/new', {
    // Specify the other units that are required for this test.
    needs: ['service:config','service:display','service:auth']
  });

  it('exists', function() {
    let route = this.subject();
    expect(route).to.be.ok;
  });
});
