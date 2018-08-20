import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupComponentTest } from 'ember-mocha';
import hbs from 'htmlbars-inline-precompile';

describe('Integration | Helper | grid-step-modulo', function() {
  setupComponentTest('grid-step-modulo', {
    integration: true
  });

  it('returns "measure" at the top of a measure', function() {
    this.render(hbs`{{grid-step-modulo 16 4 4}}`);
    expect(this.$().text().trim()).to.equal('measure');
  });

  it('returns "beat" at the top of a beat', function() {
    this.render(hbs`{{grid-step-modulo 12 4 4}}`);
    expect(this.$().text().trim()).to.equal('beat');
  });

  it('returns "step" at the top of a step', function() {
    this.render(hbs`{{grid-step-modulo 11 4 4}}`);
    expect(this.$().text().trim()).to.equal('step');
  });
});

