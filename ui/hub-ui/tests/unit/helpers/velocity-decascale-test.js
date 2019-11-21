//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('VelocityDecascaleHelper', function () {
  setupTest();

  it('scales velocity to expected values', function () {
    let velocityDecascale = this.owner.lookup('helper:velocity-decascale');
    expect(velocityDecascale.compute([0.9112])).to.equal(9);
    expect(velocityDecascale.compute([0.0])).to.equal(0);
    expect(velocityDecascale.compute([0.05])).to.equal(1);
    expect(velocityDecascale.compute([0.17])).to.equal(2);
    expect(velocityDecascale.compute([0.74])).to.equal(7);
  });

});

