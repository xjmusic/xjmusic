import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('Unit | Controller | accounts/one/libraries/one/sequences/one/patterns/one/events/one/clone', function() {
  setupTest('controller:accounts/one/libraries/one/sequences/one/patterns/one/events/one/clone', {
    needs: ['service:config']
  });

  // Replace this with your real tests.
  it('exists', function() {
    let controller = this.subject();
    expect(controller).to.be.ok;
  });
});
