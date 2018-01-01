import {moduleFor, test} from 'ember-qunit';
import Moment from "moment";

moduleFor('service:player', 'Unit | Service | player', {
  // Specify the other units that are required for this test.
  needs: ['service:config', 'service:display', 'service:binaryResource', 'service:linkScroll']
});

window.AudioContext = function () {
};

//
test('it exists', function (assert) {

  let service = this.subject();
  assert.ok(service);
});

//
test('it plays a production chain with no end time from seconds now UTC', function (assert) {

  let service = this.subject();
  let chain = {
    get: function (arg) {
      console.debug('chain.get(', arg, ') was called');
      switch (arg) {
        case 'type':
          return 'Production';
        case 'startAt':
          return '2017-11-30 05:23:50.087000';
        default:
          return null;
      }
    }
  };

  service.play(chain, null);

  let expectSeconds = nowSecondsUTC();
  let actualSeconds = service.get('playFromSecondsUTC');
  console.info({
    expectSeconds: expectSeconds,
    actualSeconds: actualSeconds
  });
  assert.expect(0);

  // in order for this to become a real test,
  // somehow follow the player.play() internal promises
  //  self.stop().then(() => { ... things ... }

/*
  assert.ok(Math.abs(expectSeconds - actualSeconds) < 2);
  assert.equal('', service.get('playFromContextTime'));
  assert.equal('', service.get('currentChain'));
  assert.equal('', service.get('currentLink'));
  assert.equal('', service.get('state'));
*/
});

function nowSecondsUTC() {
  return Math.floor(Moment.utc().valueOf() / MILLIS_PER_SECOND);
}

const MILLIS_PER_SECOND = 1000;
